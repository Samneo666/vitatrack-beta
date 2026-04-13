package web.product.vo;

import java.sql.Timestamp;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "product_image")
@Data
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private Integer imageId;

	@Column(name = "sku")
	private String sku;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "is_main")
	private Boolean isMain;

	@Column(name = "sort_order")
	private Integer sortOrder;

	@Column(name = "created_at", updatable = false)
	private Timestamp createdAt;
}
