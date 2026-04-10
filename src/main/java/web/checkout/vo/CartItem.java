package web.checkout.vo;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "order_id")
    private Integer orderId; // 允許 null：表示還在購物車

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createDate;

    @Column(name = "updated_date", insertable = false, updatable = false)
    private Timestamp updatedDate;
    /**
     * ORM 關聯：用 cart_item.sku 去對 product.sku
     * insertable/updatable=false 是重點：
     * - 真正寫入 DB 的 sku 由上面的 sku 欄位負責
     * - 這個 product 欄位只拿來 join / 查資料用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", referencedColumnName = "sku", insertable = false, updatable = false)
    private web.product.vo.Product product;

	
}
