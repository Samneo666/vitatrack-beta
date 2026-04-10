package web.checkout.vo;

import java.math.BigDecimal;

public class CartRow {

    private int cartItemId;
    private String sku;
    private String productName;
    private BigDecimal unitPrice; 
    private int quantity;

    // 建構子
    public CartRow(int cartItemId, String sku, String productName, BigDecimal unitPrice, int quantity) {
        this.cartItemId = cartItemId;
        this.sku = sku;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // ===== getters =====
    public int getCartItemId() {
        return cartItemId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
