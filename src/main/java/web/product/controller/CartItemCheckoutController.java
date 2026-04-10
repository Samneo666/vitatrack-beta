package web.product.controller;


import java.util.ArrayList;
import java.util.List;


import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import core.exception.BusinessException;
import web.member.vo.Member;
import web.product.service.CartItemService;
import web.product.vo.CartItem;
import web.product.vo.CartItemRequest;
import web.product.vo.CheckoutRequest;
import web.product.vo.CheckoutResponse;

@RestController
@RequestMapping("/cart-item")
public class CartItemCheckoutController {

    @Autowired
    private CartItemService cartItemService;

    @PostMapping("/checkout")
    public CheckoutResponse checkOut(@RequestBody CheckoutRequest checkoutRequest,
                                     HttpSession session) {

        if (checkoutRequest == null || checkoutRequest.getItems() == null || checkoutRequest.getItems().isEmpty()) {
            throw new BusinessException("購物車資料不可為空");
        }

        Member loginMember = (Member) session.getAttribute("member");
        if (loginMember == null) {
            throw new BusinessException("請先登入");
        }

        List<CartItem> cartItems = new ArrayList<>();

        for (CartItemRequest reqItem : checkoutRequest.getItems()) {
            CartItem item = new CartItem();
            item.setMemberId(loginMember.getMemberId());
            item.setSku(reqItem.getSku());
            item.setQuantity(reqItem.getQuantity());
            cartItems.add(item);
        }

        boolean success = cartItemService.saveCartItems(cartItems);

        return new CheckoutResponse(
                success,
                success ? "cart_item 寫入成功" : "cart_item 寫入失敗"
        );
    }
}