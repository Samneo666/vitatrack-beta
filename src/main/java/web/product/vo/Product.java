package web.product.vo;

import javax.persistence.Column;
import javax.persistence.Entity;

import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "product")
@Data
public class Product {
	@Id
	private String sku;
	@Column(name = "category_id")
	private Integer categoryId;
	@Column(name = "product_name")
	private String productName;
	@Column(name = "size")
	private String size;
	@Column(name = "price")
	private BigDecimal price;
	@Column(name = "stock_quantity")
	private Integer stockQuantity;
	@Column(name = "status")
	private String status;
	@Column(name = "short_description")
	private String shortDescription;
	@Column(name = "description")
	private String description;
	@Column(name = "created_by_admin_id")
	private Long createdByAdminId;
	@Column(name = "updated_by_admin_id")
	private Long updatedByAdminId;

}
