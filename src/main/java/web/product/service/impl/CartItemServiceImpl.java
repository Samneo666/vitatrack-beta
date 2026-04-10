package web.product.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import web.product.dao.CartItemDao;

import web.product.service.CartItemService;
import web.product.vo.CartItem;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {
	@Autowired
    private CartItemDao dao ;

    @Override
    public boolean saveCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            CartItem exist = dao.selectByMemberIdAndSku(item.getMemberId(), item.getSku());

            if (exist != null) {
                exist.setQuantity(exist.getQuantity() + item.getQuantity());
                int updateResult = dao.update(exist);
                if (updateResult != 1) {
                    return false;
                }
            } else {
                int insertResult = dao.insert(item);
                if (insertResult != 1) {
                    return false;
                }
            }
        }
        return true;
    }
}