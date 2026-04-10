package web.checkout.vo;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDTO {
	private Integer orderId;
	private String paymentStatus;
	private BigDecimal totalAmount;
	private Timestamp paymentTime;
	private String paymentMethod;
	private String failureReason;

	

	

}
