package ticklock.domain;

public class Event {

    private final long id;
    private final String name;
    private final int totalSeats;
    private int remainingSeats;

    public Event(long id, String name, int totalSeats) {
        if (totalSeats < 0) {
            throw new IllegalArgumentException("전체 좌석 수는 0 이상이어야 합니다.");
        }
        this.id = id;
        this.name = name;
        this.totalSeats = totalSeats;
        this.remainingSeats = totalSeats;
    }

    public long getId() {
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

    public boolean hasRemainingSeats() {
        return remainingSeats > 0;
    }

    public void decreaseSeat() {
        remainingSeats--;
    }
}