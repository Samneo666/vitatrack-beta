package web.checkout.service;

import java.util.List;

import web.checkout.vo.CartRow;
import web.checkout.vo.CheckoutResult;

public interface CheckoutService {

    CheckoutResult checkout(int memberId);
    
    // 將購物車內容呈現在結帳付款頁面
    List<CartRow> getCheckoutCart(int memberId);

}
