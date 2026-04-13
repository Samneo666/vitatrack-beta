package web.product.dao;

import java.util.List;

import web.product.vo.ProductImage;

public interface ProductImageDao {

	List<ProductImage> selectBySku(String sku);
}
