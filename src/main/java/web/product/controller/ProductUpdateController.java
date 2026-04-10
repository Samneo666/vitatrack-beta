package web.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import core.exception.BusinessException;
import web.product.service.ProductService;

import web.product.vo.Product;

@RestController
public class ProductUpdateController {

	@Autowired
	private ProductService productService;

	@PostMapping("/product-update")
	public ResponseEntity<Boolean> updateProduct(@RequestBody Product product) {

		if (product.getSku() == null || product.getSku().trim().isEmpty()) {

			throw new BusinessException("更新失敗：沒有該商品編號");
		}
		boolean result = productService.update(product);

		if (!result) {

			throw new BusinessException("更新失敗：找不到該商品", HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(result);

	}

}
