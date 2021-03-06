package it.webookia.backend.controller.resources;

import it.webookia.backend.controller.resources.exception.ResourceErrorType;
import it.webookia.backend.controller.resources.exception.ResourceException;
import it.webookia.backend.descriptor.Descriptor;
import it.webookia.backend.descriptor.DescriptorFactory;
import it.webookia.backend.descriptor.ListDescriptor;
import it.webookia.backend.descriptor.LoanFeedbackDescriptor;
import it.webookia.backend.descriptor.MessageDescriptor;
import it.webookia.backend.enums.BookStatus;
import it.webookia.backend.enums.LoanStatus;
import it.webookia.backend.enums.NotificationType;
import it.webookia.backend.model.ConcreteBook;
import it.webookia.backend.model.Feedback;
import it.webookia.backend.model.Loan;
import it.webookia.backend.model.Message;
import it.webookia.backend.model.UserEntity;
import it.webookia.backend.utils.storage.Mark;
import it.webookia.backend.utils.storage.PermissionManager;
import it.webookia.backend.utils.storage.StorageException;
import it.webookia.backend.utils.storage.StorageFacade;
import it.webookia.backend.descriptor.LoanDescriptor;

/**
 * Class to manage loan entities.
 * 
 */
public class LoanResource {

    private static StorageFacade<Loan> loanStorage = new StorageFacade<Loan>(
        Loan.class);
    private static StorageFacade<Message> messageStorage =
        new StorageFacade<Message>(Message.class);
    private static StorageFacade<Feedback> feedbackStorage =
        new StorageFacade<Feedback>(Feedback.class);
    private static StorageFacade<ConcreteBook> bookStorage =
        new StorageFacade<ConcreteBook>(ConcreteBook.class);

    private Loan decoratedLoan;

    /**
     * Class constructor
     * 
     * @param loan
     */
    LoanResource(Loan loan) {
        this.decoratedLoan = loan;
    }

    // Class methods
    /**
     * Creates a new loan between a {@link UserEntity} and the owner of a
     * {@link ConcreteBook} at the initial stage.
     * 
     * @param requestor
     *            - the {@link UserResource} which holds the user who requested
     *            the load;
     * @param book
     *            - the {@link BookResource} which holds the requested book.
     * @return a {@link LoanResource} holding the created loan.
     * @throws ResourceException
     *             if an error occurs.
     */
    public static LoanResource createLoan(UserResource requestor,
            BookResource bookRes) throws ResourceException {
        assertLoggedIn(requestor);

        ConcreteBook book = bookRes.getEntity();
        String bookId = book.getId();

        if (!PermissionManager.user(requestor.getEntity()).canBorrow(book)) {
            String message = "you cannot borrow book " + bookId;
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        // Create loan
        Loan loan = new Loan();
        loan.setLentBook(book);
        loan.setOwner(book.getOwner());
        loan.setBorrower(requestor.getEntity());
        loan.setStatus(LoanStatus.INITIAL);
        loanStorage.persist(loan);

        // Send notification
        UserResource owner = new UserResource(book.getOwner());
        NotificationResource.createNotification(
            owner,
            NotificationType.NEW_LOAN_REQUEST,
            loan);

        return new LoanResource(loan);
    }

    /**
     * Retrieves a loan from the storage.
     * 
     * @param id
     *            - the loan id.
     * @return a {@link LoanResource} holding the created loan.
     * @throws ResourceException
     *             if no corresponding loan is found.
     */
    public static LoanResource getLoan(UserResource requestor, String id)
            throws ResourceException {
        assertLoggedIn(requestor);

        Loan loan;
        try {
            loan = loanStorage.get(id);
        } catch (StorageException e) {
            throw new ResourceException(ResourceErrorType.NOT_FOUND, e);
        }

        if (loan == null) {
            String message = "loan " + id + " not found";
            throw new ResourceException(ResourceErrorType.NOT_FOUND, message);
        }

        LoanResource loanRes = new LoanResource(loan);
        if (!PermissionManager.user(getEntityFrom(requestor)).canAccess(loan)) {
            String message = "Cannot access loan " + id;
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }
        return loanRes;
    }

    /**
     * Replies to a loan request. Only the book owner can perform this action.
     * If he accepts, the {@link LoanStatus} is set to ACCEPTED and the
     * {@link BookStatus} is set to LENT, preventing further loans of the same
     * book as long as the book isn't returned. If the owner refuses, the loan
     * is marked as REFUSED.
     * 
     * @param requestor
     *            - a {@link UserResource} that manages the user who took this
     *            action.
     * @param response
     *            - the owner's response
     * @throws ResourceException
     *             if an error occurs.
     */
    public void respond(UserResource requestor, boolean response)
            throws ResourceException {
        assertLoggedIn(requestor);

        UserEntity bookOwner = decoratedLoan.getLentBook().getOwner();
        ConcreteBook lentBook = decoratedLoan.getLentBook();

        if (!requestor.matches(bookOwner)) {
            String message = "You must be the book owner to accept";
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        // Assert statuses
        assertBookStatus(lentBook, BookStatus.AVAILABLE);
        assertLoanStatus(decoratedLoan, LoanStatus.INITIAL);

        // Modify resource statuses
        if (response == true) {
            changeBookStatus(lentBook, BookStatus.LENT);
            decoratedLoan.setStatus(LoanStatus.ACCEPTED);

            // Send notification
            UserResource borrower =
                new UserResource(decoratedLoan.getBorrower());
            NotificationResource.createNotification(
                borrower,
                NotificationType.LOAN_ACCEPTED,
                decoratedLoan);

        } else {
            decoratedLoan.setStatus(LoanStatus.REFUSED);
        }
        loanStorage.persist(decoratedLoan);
    }

    /**
     * 
     * Marks that the borrower has received the book. Only the borrower can mark
     * the book as received.
     * 
     * @param requestor
     *            - the {@link UserResource} that manages the user who took this
     *            action.
     * @throws ResourceException
     *             if an error occurs.
     */
    public void bookReceived(UserResource requestor) throws ResourceException {
        assertLoggedIn(requestor);

        UserEntity borrower = decoratedLoan.getBorrower();
        ConcreteBook lentBook = decoratedLoan.getLentBook();

        if (!requestor.matches(borrower)) {
            String message = "you must be the borrower to do this";
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        assertBookStatus(lentBook, BookStatus.LENT);
        assertLoanStatus(decoratedLoan, LoanStatus.ACCEPTED);
        decoratedLoan.setStatus(LoanStatus.SHIPPED);
        loanStorage.persist(decoratedLoan);

        // Send notification
        UserResource owner = new UserResource(lentBook.getOwner());
        NotificationResource.createNotification(
            owner,
            NotificationType.LOAN_SHIPPED,
            decoratedLoan);
    }

    /**
     * Marks that the book has been given back to its owner. Only the owner can
     * perform this action.
     * 
     * @param requestor
     *            - the {@link UserResource} that manages the user who took this
     *            action.
     * @throws ResourceException
     *             if an error occurs.
     */
    public void bookReturned(UserResource requestor) throws ResourceException {
        assertLoggedIn(requestor);

        ConcreteBook lentBook = decoratedLoan.getLentBook();
        UserEntity bookOwner = lentBook.getOwner();

        if (!requestor.matches(bookOwner)) {
            String message = "You must be the book owner to accept";
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        assertBookStatus(lentBook, BookStatus.LENT);
        assertLoanStatus(decoratedLoan, LoanStatus.SHIPPED);
        changeBookStatus(lentBook, BookStatus.AVAILABLE);
        decoratedLoan.setStatus(LoanStatus.GIVEN_BACK);
        loanStorage.persist(decoratedLoan);

        // Send notification
        UserResource borrower = new UserResource(decoratedLoan.getBorrower());
        NotificationResource.createNotification(
            borrower,
            NotificationType.LOAN_GIVEN_BACK,
            decoratedLoan);
    }

    /**
     * Sends a {@link Message} to the other person involved in a loan. Only the
     * borrower and the owner can send messages within a loan.
     * 
     * @param author
     *            - the message author.
     * @param message
     *            - the text of the message.
     * @throws ResourceException
     *             if an error occurs.
     */
    public void sendContextMessage(UserResource author, String messageText)
            throws ResourceException {
        assertLoggedIn(author);

        if (!PermissionManager.user(author.getEntity()).canSendMessage(
            decoratedLoan)) {
            String message = "You must be involved in the loan to do this";
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        Message msg = new Message();
        msg.setAuthor(author.getEntity());
        msg.setLoan(decoratedLoan);
        msg.setText(messageText);
        messageStorage.persist(msg);

        // Send notification to other customer
        ConcreteBook book = decoratedLoan.getLentBook();
        UserEntity owner = book.getOwner();
        UserEntity borrower = decoratedLoan.getBorrower();

        // Send notification
        UserResource target =
            new UserResource(author.matches(owner) ? borrower : owner);
        NotificationResource.createNotification(
            target,
            NotificationType.NEW_LOAN_MESSAGE,
            decoratedLoan);
    }

    /**
     * Adds a {@link Feedback} to the loan. Only the owner and the borrower
     * involved in a {@link Loan} can add a feedback.
     * 
     * @param requestor
     *            - a {@link UserResource} managing the author of the feedback.
     * @param intMark
     *            - integer representing the mark of the feedback
     * @param text
     *            - the feedback text
     * @throws ResourceException
     *             if an error occurs.
     */
    public void addFeedback(UserResource requestor, int intMark, String text)
            throws ResourceException {
        assertLoggedIn(requestor);

        assertLoanStatus(decoratedLoan, LoanStatus.GIVEN_BACK);
        UserEntity borrower = decoratedLoan.getBorrower();
        UserEntity owner = decoratedLoan.getLentBook().getOwner();
        Mark mark;

        try {
            mark = Mark.create(intMark);
        } catch (IllegalArgumentException e) {
            throw new ResourceException(ResourceErrorType.BAD_REQUEST, e);
        }

        Feedback feedback = new Feedback();
        feedback.setMark(mark);
        feedback.setText(text);

        if (requestor.matches(borrower)) {
            if (decoratedLoan.getBorrowerFeedback() != null) {
                throw new ResourceException(
                    ResourceErrorType.ALREADY_EXSISTING,
                    "Already has borrower's feedback.");
            }

            decoratedLoan.setBorrowerFeedback(feedback); // Feedback released by
                                                         // the borrower
        } else if (requestor.matches(owner)) {
            if (decoratedLoan.getOwnerFeedback() != null) {
                throw new ResourceException(
                    ResourceErrorType.ALREADY_EXSISTING,
                    "Already has borrower's feedback.");
            }
            decoratedLoan.setOwnerFeedback(feedback);
        } else {
            String message = "You must be involved in the load to do this";
            throw new ResourceException(
                ResourceErrorType.UNAUTHORIZED_ACTION,
                message);
        }

        feedbackStorage.persist(feedback);
        loanStorage.persist(decoratedLoan);

        // Send notification
        UserResource target =
            new UserResource(requestor.matches(owner) ? borrower : owner);
        NotificationResource.createNotification(
            target,
            NotificationType.LOAN_FEEDBACK_ADDED,
            decoratedLoan);
    }

    /**
     * Creates a descriptor for managed book.
     * 
     * @return a {@link Descriptor} that describes the managed book.
     */
    public LoanDescriptor getDescriptor() {
        return DescriptorFactory.createLoanDescriptor(decoratedLoan);
    }

    /**
     * Retrieves a descriptor of the list of messages exchanged by involved
     * customer in the context of this loan.
     * 
     * @return a {@link Descriptor} containing the list of messages.
     */
    public ListDescriptor<MessageDescriptor> getMessages() {
        return DescriptorFactory.createMessageList(decoratedLoan.getMessages());
    }

    /**
     * Retrieves the feedbacks added in the context of this loan, both by owner
     * and borrower.
     * 
     * @return a {@link Descriptor} containing feedback added by involved
     *         customers.
     */

    public LoanFeedbackDescriptor getFeedbacks() {
        return DescriptorFactory.createFeedbackDescriptor(decoratedLoan);
    }

    /**
     * Retrieves this Loan
     * 
     * @return {@decoratedLoan} which contains loan details
     */
    Loan getEntity() {
        return decoratedLoan;
    }

    /**
     * Verifies if a given status is compliant with the status of the book
     * present in the db.
     * 
     * @param book
     *            a given book
     * @param status
     *            the status to be checked
     * @throws {@ResourceException} if the status is not
     *         compliant with a status format.
     */
    private void assertBookStatus(ConcreteBook book, BookStatus status)
            throws ResourceException {
        BookStatus actualStatus = book.getStatus();
        if (!actualStatus.equals(status)) {
            String message =
                "Book status assertion failed, "
                    + status
                    + " / "
                    + actualStatus;
            throw new ResourceException(
                ResourceErrorType.ILLEGAL_STATE,
                message);
        }
    }

    /**
     * Verifies if a given status is compliant with the status of the loan
     * present in the db.
     * 
     * @param loan
     *            a given loan
     * @param status
     *            the status to be checked
     * @throws {@ResourceException} if the status is not
     *         compliant with a status format.
     */
    private void assertLoanStatus(Loan loan, LoanStatus status)
            throws ResourceException {
        LoanStatus actualStatus = loan.getStatus();
        if (!actualStatus.equals(status)) {
            String message =
                "Book status assertion failed, "
                    + status
                    + " / "
                    + actualStatus;
            throw new ResourceException(
                ResourceErrorType.ILLEGAL_STATE,
                message);
        }
    }

    private static void assertLoggedIn(UserResource user)
            throws ResourceException {
        if (user == null) {
            throw new ResourceException(
                ResourceErrorType.NOT_LOGGED_IN,
                "You need to be logged in.");
        }
    }

    private void changeBookStatus(ConcreteBook book, BookStatus status) {
        book.setStatus(status);
        bookStorage.persist(book);
    }

    private static UserEntity getEntityFrom(UserResource res) {
        return res == null ? null : res.getEntity();
    }
}
