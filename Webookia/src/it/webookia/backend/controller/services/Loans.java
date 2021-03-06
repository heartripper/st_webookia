package it.webookia.backend.controller.services;

import java.io.IOException;

import javax.servlet.ServletException;

import it.webookia.backend.controller.resources.BookResource;
import it.webookia.backend.controller.resources.LoanResource;
import it.webookia.backend.controller.resources.UserResource;
import it.webookia.backend.controller.resources.exception.ResourceErrorType;
import it.webookia.backend.controller.resources.exception.ResourceException;
import it.webookia.backend.controller.services.impl.Jsp;
import it.webookia.backend.controller.services.impl.Service;
import it.webookia.backend.controller.services.impl.ServiceContext;
import it.webookia.backend.controller.services.impl.ServiceServlet;
import it.webookia.backend.controller.services.impl.Verb;
import it.webookia.backend.utils.servlets.Context;

/**
 * This is the servlet which manages loans, allowing retrieving of the complete
 * list of loans related to a user, loan's creation and its details.
 */
public class Loans extends ServiceServlet {

    private static final long serialVersionUID = 2335276930451134433L;

    public static final String CONTEXT_LOAN_ID = "id";
    public static final String CONTEXT_LOAN = "CONTEXT_LOAN";
    public static final String SENT_LOANS = "SENT_LOANS";
    public static final String RECEIVED_LOANS = "RECEIVED_LOANS";

    /**
     * Class constructor
     */
    public Loans() {
        super(Context.LOANS);
        registerDefaultService(Verb.GET, new LoanLanding());
        registerService(Verb.POST, "create", new LoanCreation());
        registerService(Verb.GET, "detail", new LoanDetail());
    }

    /**
     * This class implements the service that allows a user to create a loan.
     * 
     */
    public class LoanCreation implements Service {
        @Override
        public void service(ServiceContext context) throws ServletException,
                IOException {
            String bookId = context.getRequestParameter("bookId");

            if (!context.isUserLoggedIn()) {
                context.sendError(new ResourceException(
                    ResourceErrorType.NOT_LOGGED_IN,
                    "You need to be logged in to create a loan."));
                return;
            }

            String userId = context.getAuthenticatedUserId();
            try {
                UserResource requestor = UserResource.getUser(userId);
                BookResource book = BookResource.getBook(bookId, requestor);
                LoanResource loan = LoanResource.createLoan(requestor, book);
                String loanId = loan.getDescriptor().getId();
                String newUrl = "/loan/detail?id=" + loanId;
                context.sendRedirect(newUrl);
            } catch (ResourceException e) {
                context.sendError(e);
                return;
            }
        }
    }

    /**
     * This class implements the service that allows a user to retrieve a list
     * of loans in which he is involved.
     * 
     */
    public class LoanLanding implements Service {

        @Override
        public void service(ServiceContext context) throws ServletException,
                IOException {
            String userId = context.getAuthenticatedUserId();

            if (!context.isUserLoggedIn()) {
                context.sendError(new ResourceException(
                    ResourceErrorType.NOT_LOGGED_IN,
                    "You need to be logged in to access your loans."));
                return;
            }

            try {
                UserResource user = UserResource.getUser(userId);
                context.setRequestAttribute(SENT_LOANS, user
                    .getSentLoanRequest(-1)
                    .getElements());
                context.setRequestAttribute(RECEIVED_LOANS, user
                    .getReceivedLoanRequest(-1)
                    .getElements());
                context.forwardToJsp(Jsp.LOAN_JSP);
            } catch (ResourceException e) {
                context.sendError(e);
            }

        }
    }

    /**
     * This class implements the service that allows a user to open a specific
     * loan in which he is involved.
     * 
     */
    public class LoanDetail implements Service {

        @Override
        public void service(ServiceContext context) throws ServletException,
                IOException {
            String loanId = context.getRequestParameter(CONTEXT_LOAN_ID);
            String requestorId = context.getAuthenticatedUserId();
            try {
                UserResource requestor = UserResource.getUser(requestorId);
                LoanResource loan = LoanResource.getLoan(requestor, loanId);
                context.setRequestAttribute(CONTEXT_LOAN, loan);
                context.forwardToJsp(Jsp.LOAN_DETAIL);
            } catch (ResourceException e) {
                context.sendError(e);
            }
        }
    }

}
