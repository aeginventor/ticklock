package ticklock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int totalSeats;

    private int remainingSeats;

    @Version
    private Long version;

    protected EventEntity() {
    }

    public EventEntity(String name, int totalSeats) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.remainingSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getRemainingSeats() {
        return remainingSeats;
    }

    public Long getVersion() {
        return version;
    }

    public boolean hasRemainingSeats() {
        return remainingSeats > 0;
    }

    public void decreaseSeat() {
        if (remainingSeats <= 0) {
            throw new IllegalStateException("남은 좌석이 없습니다.");
        }
        this.remainingSeats--;
    }
}

