package ticklock.controller.dto;

public record PurchaseRequest(
        Integer quantity
) {
    public PurchaseRequest {
        if (quantity == null) {
            quantity = 1;
        }
    }
}

