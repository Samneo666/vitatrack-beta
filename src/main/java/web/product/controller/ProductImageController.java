package web.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.exception.BusinessException;
import web.product.service.ProductImageService;
import web.product.vo.ProductImage;

@RestController
public class ProductImageController {

	@Autowired
	private ProductImageService productImageService;

	@GetMapping("/product-images")
	public ResponseEntity<List<ProductImage>> getProductImages(@RequestParam(value = "sku") String sku) {
		if (sku == null || sku.isBlank()) {
			throw new BusinessException("SKU 不能為空");
		}

		List<ProductImage> images = productImageService.findBySku(sku);

		if (images == null || images.isEmpty()) {
			throw new BusinessException("查無此 SKU 的圖片", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(images);
	}
}
