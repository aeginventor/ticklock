package ticklock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_types")
public class TicketTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private int totalSeats;

    private int remainingSeats;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    protected TicketTypeEntity() {
    }

    public TicketTypeEntity(String name, int price, int totalSeats, EventEntity event) {
        this.name = name;
        this.price = price;
        this.totalSeats = totalSeats;
        this.remainingSeats = totalSeats;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
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

    public EventEntity getEvent() {
        return event;
    }

    public boolean hasRemainingSeats() {
        return remainingSeats > 0;
    }

    public void decreaseSeat() {
        if (remainingSeats <= 0) {
            throw new IllegalStateException("남은 좌석이 없습니다: " + name);
        }
        this.remainingSeats--;
    }
}