package web.product.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.product.dao.ProductDao;

import web.product.service.ProductService;
import web.product.vo.Product;

@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductDao productDao;

	@Override
	public boolean add(Product product) {
		if (product == null) {
			return false;
		}
		// 商品名稱
		if (product.getProductName() == null) {
			return false;
		}
		// 價格 > 0
		if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
			return false;
		}
		// 顆數 > 0
		if (product.getSize() == null || product.getSize().trim().isEmpty()) {
			return false;
		}
		// 商品數量 > 0
		if (product.getStockQuantity() <= 0) {
			return false;
		}
		// 商品描述
		if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
			return false;
		}
		// 商品簡述
		if (product.getShortDescription() == null || product.getShortDescription().trim().isEmpty()) {
			return false;
		}
		// 商品狀態是否為上架
		if (product.getStatus() == null || product.getShortDescription().trim().isEmpty()) {
			return false;
		}

		product.setSku(generateSku());
		product.setCreatedByAdminId(1L);
		product.setCategoryId(1);

		// 驗證通過才寫入資料庫
		return productDao.insert(product);
	}

	// 自動取sku編號
	private String generateSku() {
		String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());

		String randomPart = String.valueOf((int) (Math.random() * 9000) + 1000);

		return datePart + randomPart;
	}

	@Override
	public List<Product> selectAll() {
		return productDao.selectAll();
	}

	@Override
	public boolean update(Product product) {

		if (product == null) {
			return false;
		}

		if (product.getSku() == null || product.getSku().trim().isEmpty()) {
			return false;
		}
		if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
			return false;
		}
		if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			return false;
		}
		if (product.getSize() == null || product.getSize().trim().isEmpty()) {
			return false;
		}
		if (product.getStockQuantity() <= 0) {
			return false;
		}
		if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
			return false;
		}
		if (product.getShortDescription() == null || product.getShortDescription().trim().isEmpty()) {
			return false;
		}
		if (product.getStatus() == null) {
			return false;
		}

		boolean result = productDao.updateEditableFields(product);

		return result;

	}

	@Override
	public boolean deleteBySku(String sku) {				
		return productDao.deleteBySku(sku);
	
	}

	@Override
	public List<Product> findAll() {
		return productDao.selectAll();
	}

	@Override
	public Product selectBySku(String sku) {
	    // 使用 Spring 內建工具類檢查字串，簡潔且安全
	    if (sku == null || sku.isBlank()) { 
	        return null;
	    }
	    return productDao.selectBySku(sku);
	}
	
	public List<Product> selectRelatedBySku(String sku, int size) {

		Product currentProduct = productDao.selectBySku(sku);

		if (currentProduct == null) {
			return new ArrayList<>();
		}

		return productDao.selectRelated(currentProduct.getSku(), currentProduct.getCategoryId(), size);
	}

	@Override
	public List<Product> selectBySkus(List<String> skus) {
		if (skus == null || skus.isEmpty()) {
			return new ArrayList<>();
		}
		List<Product> dbList = productDao.selectBySkus(skus);
		List<Product> result = new ArrayList<>();

		for (String sku : skus) {
			for (Product p : dbList) {
				if (sku.equals(p.getSku())) {
					result.add(p);
					break;
				}
			}
		}

		return result;
	}
	
	@Override
	public List<Product> getRelatedProducts(String sku) {
	    // 1. 先獲取當前商品資訊
	    Product currentProduct = productDao.selectBySku(sku);
	    
	    // 2. 防禦性判斷：如果找不到該商品，回傳空清單或預設推薦
	    if (currentProduct == null) {
	        return new ArrayList<>(); 
	    }

	    // 3. 根據當前商品的類別 (categoryId) 撈取相關商品
	    // 排除自己 (currentSku)，並限制回傳數量 (例如 4 筆)
	    return productDao.selectByCategory(currentProduct.getCategoryId(), sku, 4);
	}

}
