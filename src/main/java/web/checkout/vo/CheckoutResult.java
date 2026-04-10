package web.checkout.vo;

public class CheckoutResult {
    private final int orderId;
    private final int totalAmount;
    private final String status;
    
    // === 建構子 ===
    public CheckoutResult(int orderId, int totalAmount, String status) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // ===== getters =====
    public int getOrderId() {
        return orderId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
