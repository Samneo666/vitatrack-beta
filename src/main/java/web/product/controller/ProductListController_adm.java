package web.product.controller;


import java.util.List;





import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import web.product.service.ProductService;

import web.product.vo.Product;

@RestController
public class ProductListController_adm {
	@Autowired
	private ProductService productService;

	@GetMapping("/product-list")
	public ResponseEntity<List<Product>> getProductList() {
		List<Product> list = productService.selectAll();

		return ResponseEntity.ok(list);

	}

}
