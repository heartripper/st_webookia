package it.webookia.backend.model;

import it.webookia.backend.enums.LoanStatus;
import it.webookia.backend.utils.storage.Storable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.SortDirection;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;
import org.slim3.datastore.Sort;

/**
 * This class manages loans of concrete book between two users.
 *
 */
@Model(schemaVersion = 1)
public class Loan implements Serializable, Storable {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public Loan() {

        borrowerRef = new ModelRef<UserEntity>(UserEntity.class);
        ownerRef = new ModelRef<UserEntity>(UserEntity.class);
        lentBookRef = new ModelRef<ConcreteBook>(ConcreteBook.class);
        borrowerFeedbackRef = new ModelRef<Feedback>(Feedback.class);
        ownerFeedbackRef = new ModelRef<Feedback>(Feedback.class);
        date = new Date();

        messagesRef =
            new InverseModelListRef<Message, Loan>(
                Message.class,
                "relativeLoanRef",
                this,
                new Sort("date", SortDirection.DESCENDING));
    }

    @Attribute(primaryKey = true)
    private Key key;

    @Attribute(version = true)
    private Long version;

    // Attributes
    private LoanStatus status;
    private Date date;

    // Relationships
    private ModelRef<ConcreteBook> lentBookRef;
    private ModelRef<UserEntity> borrowerRef;
    private ModelRef<UserEntity> ownerRef;
    private ModelRef<Feedback> borrowerFeedbackRef;
    private ModelRef<Feedback> ownerFeedbackRef;

    @Attribute(persistent = false)
    private InverseModelListRef<Message, Loan> messagesRef;

    // Storable
    @Override
    public String getId() {
        return KeyFactory.keyToString(key);
    }

    // Getters and setters
    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    //Relationships getters and setters
    public ModelRef<ConcreteBook> getLentBookRef() {
        return lentBookRef;
    }

    public ModelRef<UserEntity> getBorrowerRef() {
        return borrowerRef;
    }

    public ModelRef<UserEntity> getOwnerRef() {
        return ownerRef;
    }

    public InverseModelListRef<Message, Loan> getMessagesRef() {
        return messagesRef;
    }

    public ModelRef<Feedback> getBorrowerFeedbackRef() {
        return borrowerFeedbackRef;
    }

    public ModelRef<Feedback> getOwnerFeedbackRef() {
        return ownerFeedbackRef;
    }

    public UserEntity getBorrower() {
        return borrowerRef.getModel();
    }

    public void setBorrower(UserEntity borrower) {
        borrowerRef.setModel(borrower);
    }

    public UserEntity getOwner() {
        return ownerRef.getModel();
    }

    public void setOwner(UserEntity owner) {
        ownerRef.setModel(owner);
    }

    public ConcreteBook getLentBook() {
        return lentBookRef.getModel();
    }

    public void setLentBook(ConcreteBook lentBook) {
        lentBookRef.setModel(lentBook);
    }

    public List<Message> getMessages() {
        return messagesRef.getModelList();
    }

    public Feedback getBorrowerFeedback() {
        return borrowerFeedbackRef.getModel();
    }

    public void setBorrowerFeedback(Feedback feedback) {
        borrowerFeedbackRef.setModel(feedback);
    }

    public Feedback getOwnerFeedback() {
        return ownerFeedbackRef.getModel();
    }

    public void setOwnerFeedback(Feedback feedback) {
        ownerFeedbackRef.setModel(feedback);
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Loan other = (Loan) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
}
