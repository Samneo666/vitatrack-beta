package web.product.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.exception.BusinessException;
import web.product.service.ProductService;



@RestController
public class ProductDeleteController {

	@Autowired
	private ProductService productService;

	@PostMapping("/product-delete")
	public ResponseEntity<Boolean> deleteProduct(@RequestBody Map<String, String> body) {
		String sku = body.get("sku");

		if (sku == null || sku.isBlank()) {
			throw new BusinessException("無該商品編號");
		}
		boolean ok = productService.deleteBySku(sku.trim());

		return ResponseEntity.ok(ok);

	}

}
