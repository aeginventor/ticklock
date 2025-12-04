package ticklock.controller.dto;

public record PurchaseResponse(
        boolean success,
        String message,
        int remainingSeats
) {
    public static PurchaseResponse success(int remainingSeats) {
        return new PurchaseResponse(true, "구매 성공", remainingSeats);
    }

    public static PurchaseResponse failure(String message) {
        return new PurchaseResponse(false, message, -1);
    }
}

