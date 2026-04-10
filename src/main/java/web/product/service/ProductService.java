package web.product.service;

import java.util.List;

import web.product.vo.Product;

public interface ProductService {
	boolean add(Product product);

	List<Product> selectAll();

	boolean update(Product product);

	boolean deleteBySku(String sku);

	List<Product> findAll();
	
	List<Product> selectBySkus(List<String> skus);

	Product selectBySku(String sku);
}
