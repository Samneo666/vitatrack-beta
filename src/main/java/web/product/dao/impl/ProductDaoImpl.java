package web.product.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;


import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.product.dao.ProductDao;
import web.product.vo.Product;

@Repository
public class ProductDaoImpl implements ProductDao {

	@PersistenceContext
	Session session;

	@Override
	public List<Product> selectAll() {

		return session.createQuery("FROM Product", Product.class)
				.getResultList();
	}

	@Override
	public boolean insert(Product product) {

		session.persist(product);
		return true;
	}

	@Override
	public boolean updateEditableFields(Product product) {

		// 先用 sku 找到「資料庫那筆」
		Product dbProduct = session.get(Product.class, product.getSku());
		if (dbProduct == null) {
			return false;
		}
		dbProduct.setProductName(product.getProductName());
		dbProduct.setPrice(product.getPrice());
		dbProduct.setSize(product.getSize());
		dbProduct.setStockQuantity(product.getStockQuantity());
		dbProduct.setDescription(product.getDescription());
		dbProduct.setShortDescription(product.getShortDescription());
		if (product.getStatus() != null) {
			dbProduct.setStatus(product.getStatus());
		}
		dbProduct.setUpdatedByAdminId(1L);
		return true;
	}

	@Override
	public boolean deleteBySku(String sku) {

		Product target = session.get(Product.class, sku);
		if (target == null) {
			return false;
		}
		session.delete(target);
		return true;
	}

	@Override
	public Product selectBySku(String sku) {

		return session.get(Product.class, sku);

	}

	@Override
	public List<Product> selectRelated(String sku, Integer categoryId, Integer size) {

		final String hql = "FROM Product p " + "WHERE p.categoryId = :categoryId " + "AND p.sku != :sku "
				+ "ORDER BY p.sku DESC";

		return session.createQuery(hql, Product.class).setParameter("categoryId", categoryId).setParameter("sku", sku)
				.setMaxResults(size)
				.getResultList();
	}

	@Override
	public List<Product> selectBySkus(List<String> skus) {

		final String hql = "FROM Product p WHERE p.sku IN (:skus)";

		return session.createQuery(hql, Product.class)
				.setParameterList("skus", skus)
				.getResultList();
	}

}
