package web.checkout.vo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ResultDTO {
	private Integer orderId;
	private String paymentStatus;
	private BigDecimal totalAmount;
	private Timestamp paymentTime;
	private String paymentMethod;
	private String failureReason;

	public ResultDTO(Integer orderId, String paymentStatus, BigDecimal totalAmount, Timestamp paymentTime,
			String paymentMethod, String failureReason) {
		super();
		this.orderId = orderId;
		this.paymentStatus = paymentStatus;
		this.totalAmount = totalAmount;
		this.paymentTime = paymentTime;
		this.paymentMethod = paymentMethod;
		this.failureReason = failureReason;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Timestamp getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(Timestamp paymentTime) {
		this.paymentTime = paymentTime;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

}
