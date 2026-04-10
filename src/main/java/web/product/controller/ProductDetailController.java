package web.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



import core.exception.BusinessException;
import web.product.service.ProductService;

import web.product.vo.Product;

@RestController
public class ProductDetailController {

	@Autowired
	private ProductService productService;

	@GetMapping("/product-detail")
	public ResponseEntity<Product> getProductDetail(@RequestParam(value = "sku") String sku) {
		if (sku == null || sku.isBlank()) {
			throw new BusinessException("SKU 不能為空");
		}
		
		Product p = productService.selectBySku(sku);

		if (p == null) {
			throw new BusinessException("找不到該 SKU 的商品", HttpStatus.NOT_FOUND);
		}
		
		return ResponseEntity.ok(p);
	}
}
