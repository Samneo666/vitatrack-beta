package web.checkout.vo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Data;

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
