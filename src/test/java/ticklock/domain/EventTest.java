package ticklock.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    @DisplayName("전체 좌석 수가 음수이면 예외가 발생한다")
    void createEvent_withNegativeSeats_shouldThrowException() {
        // given
        long id = 1L;
        String name = "테스트 공연";
        int totalSeats = -1;

        // when & then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> new Event(id, name, totalSeats)
        );
        assertEquals("전체 좌석 수는 0 이상이어야 합니다.", e.getMessage());
    }

    @Test
    @DisplayName("정상 좌석 수로 생성하면 남은 좌석 수는 전체 좌석 수로 초기화된다")
    void createEvent_withValidSeats_shouldInitializeRemainingSeats() {
        // given
        long id = 1L;
        String name = "테스트 공연";
        int totalSeats = 100;

        // when
        Event event = new Event(id, name, totalSeats);

        // then
        assertEquals(totalSeats, event.getRemainingSeats());
        assertTrue(event.hasRemainingSeats());
    }
}