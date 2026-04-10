package web.product.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import web.product.service.ProductService;

import web.product.vo.Product;

@RestController
public class ProductAddController {
	@Autowired
	private ProductService productService;

	@PostMapping("/product-add")
	public boolean addProduct(@RequestBody Product product) {
		
		return productService.add(product);
	}

}
