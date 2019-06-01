import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by mtumilowicz on 2018-05-02.
 */
@Entity
public class Entitlement {

    @Id
    private int stateRef;

    private int e_id;

    private int amount;

    private int announcementId;

    public int getE_id() {
        return e_id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
    }

    public int getStateRef() {
        return stateRef;
    }

    public void setStateRef(int stateRef) {
        this.stateRef = stateRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entitlement author = (Entitlement) o;

        return e_id == author.e_id;
    }

    @Override
    public int hashCode() {
        return e_id;
    }

    @Override
    public String toString() {
        return "Author{" +
                "e_id=" + e_id +
                '}';
    }
}
