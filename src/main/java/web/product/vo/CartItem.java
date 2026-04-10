package web.product.vo;

import java.sql.Timestamp;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "ProductCartItem")
@Table(name = "cart_item")

public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_item_id")
	private Integer cartItemId;

	@Column(name = "member_id")
	private Integer memberId;

	@Column(name = "sku")
	private String sku;

	@Column(name = "quantity")
	private Integer quantity;

	@Column(name = "created_date", insertable = false, updatable = false)
	private Timestamp createdDate;

}