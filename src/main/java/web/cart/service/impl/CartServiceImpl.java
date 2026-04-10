package web.cart.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import web.cart.dao.CartDao;
import web.cart.dto.AddToCartItemResponse;
import web.cart.dto.RemoveCartItemResponse;
import web.cart.dto.UpdateCartItemResponse;
import web.cart.service.CartService;
import web.checkout.vo.CartItem;
import core.exception.BusinessException;
import web.product.dao.ProductDao;



@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartDao cartDao;
	@Autowired
	private ProductDao productDao;
	
	//加入商品
	@Transactional
	@Override
	public AddToCartItemResponse addToCart(Integer memberId, String sku, Integer quantity) {
		if (quantity == null || quantity <= 0) {
			throw new BusinessException("數量錯誤");
		}
		if (sku == null || sku.isBlank() ) {
			throw new BusinessException("此商品已售完或未上架");
		}
		
		CartItem cartItemDb = cartDao.SelectByMemberIdAndSku(memberId, sku);
		
		if (cartItemDb != null) {
			//已存在 -> 更新
			cartItemDb.setQuantity(cartItemDb.getQuantity() + quantity);
			return new AddToCartItemResponse(cartItemDb.getSku(), cartItemDb.getQuantity());
		}else {	
			//新增
			cartItemDb = new CartItem();
			cartItemDb.setMemberId(memberId);
			cartItemDb.setSku(sku);
			cartItemDb.setQuantity(quantity);
			cartDao.insert(cartItemDb);		
		}
		
		return new AddToCartItemResponse(cartItemDb.getSku(), cartItemDb.getQuantity());
	}
	
	//更新商品數量
	@Transactional
	@Override
	public UpdateCartItemResponse updateQuantity(Integer memberId, String sku, Integer quantity) {
		if (quantity == null || quantity <= 0) {
			throw new BusinessException("購買數量必須大於0");
		}
			
		//查找購物車項目
		CartItem cartItemDb = cartDao.SelectByMemberIdAndSku(memberId, sku);
		if (cartItemDb == null) {
			throw new BusinessException("購物車內無該商品!");	
		}
		
		//庫存檢查
		Integer stock = productDao.selectBySku(sku).getStockQuantity();
		if (quantity > stock) {
			throw new BusinessException("庫存不足!");
		}
		
		cartItemDb.setQuantity(quantity);
		
		return new UpdateCartItemResponse (cartItemDb.getSku(),cartItemDb.getQuantity());
		
	}
	//移除商品
	@Transactional
	@Override
	public RemoveCartItemResponse removeItem(Integer memberId, List<String> skus) {
		//防止傳入空集合導致的異常
	    if (skus == null || skus.isEmpty()) {
	        return new RemoveCartItemResponse(0);
	    }
		
		int deleteCount =cartDao.deleteByIDAndSkus(memberId,skus);
		
		if (deleteCount == 0) {
	  
	         throw new BusinessException("找不到可刪除的商品");
	    }
		return new RemoveCartItemResponse(deleteCount);
	}
	
	
	

}
