package web.product.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.product.dao.ProductImageDao;
import web.product.vo.ProductImage;

@Repository
public class ProductImageDaoImpl implements ProductImageDao {

	@PersistenceContext
	Session session;

	@Override
	public List<ProductImage> selectBySku(String sku) {
		return session.createQuery(
				"FROM ProductImage WHERE sku = :sku ORDER BY sortOrder ASC",
				ProductImage.class)
				.setParameter("sku", sku)
				.getResultList();
	}
}
