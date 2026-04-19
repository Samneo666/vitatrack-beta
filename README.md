專題名稱:VitaTrack-wellness foods E-commerce 

專題起源:
我們希望打造一個健康食品為主軸電商平台，讓使用者不只買到適合的產品，更能持續維持健康習慣。


目錄結構

vitatrack-beta/
├── pom.xml
├── src/main/java/
│   ├── core/                    # 跨模組基礎設施
│   │   ├── config/              # Spring 設定 (App, MVC, Mail, WebAppInit)
│   │   ├── dto/                 # 通用 ApiResponse<T>
│   │   ├── exception/           # GlobalExceptionHandler, BusinessException
│   │   └── interceptor/         # LoginInterceptor（Session 認證攔截器）
│   └── web/                     # 業務模組
│       ├── member/              # 會員：註冊、登入、個人資料、密碼重設
│       ├── member_admin/        # 後台管理：會員列表、搜尋、狀態編輯
│       ├── product/             # 商品 CRUD、圖片、庫存
│       ├── cart/                # 購物車：新增、更新、刪除、列表
│       └── checkout/            # 訂單建立、綠界付款、回調
├── src/main/resources/          # log4j2, mail, payment 設定
└── src/main/webapp/assets/      # 靜態前端資源


分層架構（每個模組一致）
每個業務模組都遵循嚴格的 4 層模式：
controller/   → @RestController，處理 HTTP 請求
dto/          → Request/Response DTO
service/      → Interface + impl/（@Service, @Transactional）
dao/          → Interface + impl/（使用 Hibernate SessionFactory）
vo/           → @Entity Hibernate 實體類別


認證機制

- Session-based：登入後將 member 物件存入 HttpSession
- LoginInterceptor 攔截所有 /api/** 路徑，排除公開端點（登入、註冊、密碼重設）
- 未驗證時回傳 401 Unauthorized
- 密碼使用 BCrypt雜湊
- 管理員有獨立的登入流程與 session 屬性
