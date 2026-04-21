package web.product.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import core.exception.BusinessException;
import web.cart.dao.CartDao;
import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.vo.CartRow;
import web.checkout.vo.OrderItem;
import web.checkout.vo.Orders;
import web.product.dto.OrderCreationResponse;
import web.product.dto.PlaceOrderRequest;
import web.product.service.PlaceOrderService;

@Service
public class PlaceOrderServiceImpl implements PlaceOrderService {

    private static final Logger log = LogManager.getLogger(PlaceOrderServiceImpl.class);

    @Autowired
    CartDao cartDao;

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Override
    @Transactional
    public OrderCreationResponse checkout(int memberId, PlaceOrderRequest request) {

        // 1. 查詢該會員的未結帳購物車
        List<CartRow> cartRows = cartDao.findOpenCartByMemberId(memberId);
        if (cartRows == null || cartRows.isEmpty()) {
            throw new BusinessException("購物車是空的!");
        }

        // 2. 計算總金額
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartRow row : cartRows) {
            BigDecimal rowSubtotal = row.getUnitPrice().multiply(BigDecimal.valueOf(row.getQuantity()));
            totalAmount = totalAmount.add(rowSubtotal);
        }
        int totalAmountInt = totalAmount.intValue();

        // 3. 建立 orders，初始付款狀態為 PENDING
        Orders order = new Orders();
        order.setMemberId(memberId);
        order.setTotalAmount(totalAmount);
        order.setAmount(totalAmount);
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod("ECPAY");
        order.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        // 儲存收件人資訊（若前端有傳入）
        if (request != null) {
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setReceiverAddress(request.getReceiverAddress());
        }

        orderDao.save(order);
        Integer orderId = order.getOrderId();

        // 4. 建立各筆 order_item
        for (CartRow row : cartRows) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setSku(row.getSku());
            oi.setProductName(row.getProductName());
            oi.setUnitPrice(row.getUnitPrice());
            oi.setQuantity(row.getQuantity());
            oi.setSubtotal(row.getUnitPrice().multiply(BigDecimal.valueOf(row.getQuantity())));
            orderItemDao.save(oi);
        }

        // 5. 清除購物車（刪除已下單的 cart_item）
        List<String> skus = cartRows.stream()
                .map(CartRow::getSku)
                .collect(Collectors.toList());
        int deletedCount = cartDao.deleteCartItems(memberId, skus);
        if (deletedCount <= 0) {
            log.warn("刪除購物車回傳 0 筆，memberId={}, skus={}", memberId, skus);
            throw new BusinessException("清除購物車失敗，請重試！");
        }

        log.info("訂單建立成功，orderId={}, memberId={}, totalAmount={}", orderId, memberId, totalAmountInt);

        // 6. 回傳結果（不回傳 memberId 以減少資訊洩漏）
        return new OrderCreationResponse(null, orderId, totalAmountInt, "PENDING");
    }
}
