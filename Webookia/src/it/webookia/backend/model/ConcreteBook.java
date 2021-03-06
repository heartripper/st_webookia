package it.webookia.backend.model;

import it.webookia.backend.enums.BookStatus;
import it.webookia.backend.enums.PrivacyLevel;
import it.webookia.backend.utils.storage.Storable;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.SortDirection;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;
import org.slim3.datastore.Sort;

/**
 * 
 * This class manages concrete instances of books, i.e. books owned by a user of
 * the platform.
 * 
 */
@Model(schemaVersion = 1)
public class ConcreteBook implements Serializable, Storable {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructors
     */
    public ConcreteBook() {
        //Relationships
        detailedBookRef = new ModelRef<DetailedBook>(DetailedBook.class);
        reviewRef = new ModelRef<Review>(Review.class);
        ownerRef = new ModelRef<UserEntity>(UserEntity.class);
        loansRef =
            new InverseModelListRef<Loan, ConcreteBook>(
                Loan.class,
                "lentBookRef",
                this,
                new Sort("date", SortDirection.DESCENDING));
    }

    @Attribute(primaryKey = true)
    private Key key;

    @Attribute(version = true)
    private Long version;

    // Fields
    private PrivacyLevel privacy;
    private BookStatus status;

    // Relationship
    private ModelRef<DetailedBook> detailedBookRef;
    private ModelRef<Review> reviewRef;
    private ModelRef<UserEntity> ownerRef;

    // Inverse relationship
    @Attribute(persistent = false)
    private InverseModelListRef<Loan, ConcreteBook> loansRef;

    // Storable
    @Override
    public String getId() {
        return KeyFactory.keyToString(key);
    }

    //Getters and setters
    public PrivacyLevel getPrivacy() {
        return privacy;
    }

    public void setPrivacy(PrivacyLevel privacy) {
        this.privacy = privacy;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
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

    // Relationship getters and setters
    public ModelRef<DetailedBook> getDetailedBookRef() {
        return detailedBookRef;
    }

    public ModelRef<Review> getReviewRef() {
        return reviewRef;
    }

    public ModelRef<UserEntity> getOwnerRef() {
        return ownerRef;
    }

    public InverseModelListRef<Loan, ConcreteBook> getLoansRef() {
        return loansRef;
    }

    public DetailedBook getDetailedBook() {
        return detailedBookRef.getModel();
    }

    public void setDetailedBook(DetailedBook detailedBook) {
        detailedBookRef.setModel(detailedBook);
    }

    public UserEntity getOwner() {
        return ownerRef.getModel();
    }

    public void setOwner(UserEntity owner) {
        ownerRef.setModel(owner);
    }

    public Review getReview() {
        return reviewRef.getModel();
    }

    public void setReview(Review review) {
        reviewRef.setModel(review);
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
        ConcreteBook other = (ConcreteBook) obj;
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
