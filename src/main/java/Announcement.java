import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * Created by mtumilowicz on 2018-05-02.
 */
@Entity
public class Announcement {
    @Id
    private int stateRef;
    private int a_id;

    private String title;

    private int dividend;

    private OffsetDateTime createdOn;

    public int getA_id() {
        return a_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDividend() {
        return dividend;
    }

    public void setDividend(int dividend) {
        this.dividend = dividend;
    }

    public int getStateRef() {
        return stateRef;
    }

    public void setStateRef(int stateRef) {
        this.stateRef = stateRef;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Announcement book = (Announcement) o;

        return a_id == book.a_id;
    }

    @Override
    public int hashCode() {
        return a_id;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "a_id=" + a_id +
                '}';
    }
}
