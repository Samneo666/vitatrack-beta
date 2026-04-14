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
	public ResponseEntity<List<Product>> getRelatedProducts(@RequestParam("sku") String sku) {
	    // 確保參數不為空
	    if (sku == null || sku.isBlank()) {
	        return ResponseEntity.badRequest().build();
	    }
	    
	    // 改為呼叫獲取「清單」的方法
	    List<Product> list = productService.getRelatedProducts(sku); 
	    
	    return ResponseEntity.ok(list);
	}

}