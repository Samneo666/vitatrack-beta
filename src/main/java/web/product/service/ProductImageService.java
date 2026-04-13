package web.product.service;

import java.util.List;

import web.product.vo.ProductImage;

public interface ProductImageService {

	List<ProductImage> findBySku(String sku);
}
