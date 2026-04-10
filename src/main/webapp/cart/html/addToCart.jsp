<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="modal fade" id="addToCartModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">

      <div class="modal-header">
        <h5 class="modal-title">加入購物車</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        <p>庫存數量：<span id="stockQty"></span></p>

        <div class="input-group">
          <button class="btn btn-outline-secondary" onclick="changeQty(-1)">−</button>
          <input type="number" class="form-control text-center" id="qtyInput" value="1" min="1">
          <button class="btn btn-outline-secondary" onclick="changeQty(1)">＋</button>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-primary" onclick="confirmAddToCart()">確認加入</button>
        <button class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
      </div>

    </div>
  </div>
</div>