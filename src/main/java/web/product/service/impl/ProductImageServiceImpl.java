package web.product.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.product.dao.ProductImageDao;
import web.product.service.ProductImageService;
import web.product.vo.ProductImage;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

	@Autowired
	private ProductImageDao productImageDao;

	@Override
	public List<ProductImage> findBySku(String sku) {
		return productImageDao.selectBySku(sku);
	}
}
