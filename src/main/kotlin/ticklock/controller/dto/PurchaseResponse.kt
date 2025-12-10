package ticklock.controller.dto

data class PurchaseResponse(
    val success: Boolean,
    val message: String,
    val remainingSeats: Int
) {
    companion object {
        @JvmStatic
        fun success(remainingSeats: Int): PurchaseResponse = PurchaseResponse(
            success = true,
            message = "구매 성공",
            remainingSeats = remainingSeats
        )

        @JvmStatic
        fun failure(message: String): PurchaseResponse = PurchaseResponse(
            success = false,
            message = message,
            remainingSeats = -1
        )
    }
}