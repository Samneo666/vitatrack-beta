package web.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.exception.BusinessException;
import web.product.service.ProductService;

import web.product.vo.Product;

@RestController
public class ProductRelatedController {

	@Autowired
	private ProductService productService;

	@GetMapping("/product-related")
	public ResponseEntity<List<Product>> getRelatedProducts(@RequestParam("skus") List<String> skus) {

		if (skus == null || skus.isEmpty()) {
			throw new BusinessException("請提供至少一個商品編號");
		}

		List<Product> list = productService.selectBySkus(skus);

		return ResponseEntity.ok(list);
	}

}