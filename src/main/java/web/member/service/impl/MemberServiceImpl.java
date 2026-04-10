package web.member.service.impl;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.vo.CartItem;
import web.checkout.vo.Orders;
import web.member.dao.MemberDao;

import web.member.service.MemberService;
import web.member.vo.Member;
import web.member_admin.dto.PageResultResponse;
import web.product.vo.Product;
import web.member.dto.CartItemResponse;
import web.member.dto.UpdateMemberRequest;
import core.exception.BusinessException;

@Service
public class MemberServiceImpl implements MemberService {
	@Autowired
	private MemberDao memberDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// 註冊
	@Transactional
	@Override
	public String register(Member member) {
		// 基礎驗證
		validateName(member.getName());
		validateEmail(member.getEmail());
		validatePhone(member.getPhone());
		validatePassword(member.getPassword(), member.getConfirmPassword());

		if (memberDao.selectByEmail(member.getEmail()) != null) {
			throw new BusinessException("此帳號已經被註冊了");
		}
		//電話唯一性檢查
		if (memberDao.selectByPhone(member.getPhone()) != null) {
	        throw new BusinessException("此手機號碼已經被註冊了");
	    }
		member.setAddress(validateAddress(member.getAddress()));
		// 密碼加密
		String encoded = passwordEncoder.encode(member.getPassword());
		member.setPassword(encoded);
		// 註冊方法在通過所有驗證後，呼叫 memberDao.insert(member)，新增資料。
		memberDao.insert(member);
		// 註冊成功，回傳 null 代表沒有錯誤訊息或 代表的是錯誤訊息為空」。
		return null;
	}

	// 登入
	@Transactional
	@Override
	public Member login(Member member) {
		// 驗證帳號和密碼
		String email = member.getEmail();
		String password = member.getPassword();
		if (email == null || email.isBlank() || password == null || password.isBlank()) {
			throw new BusinessException("請輸入帳號或密碼");
		}
		Member dbMember = memberDao.selectByEmail(email);
		if (dbMember == null) {
			throw new BusinessException("帳號或密碼錯誤");
		}
		
		if (dbMember.getMemberStatus() != null && dbMember.getMemberStatus() == 0) {
			throw new BusinessException("此帳號已被註銷或停用");
		}
		String dbPassword = dbMember.getPassword();
		// 資料庫儲存的雜湊值」不匹配時，判定登入失敗。
		// (使用者輸入的密碼,資料庫中儲存的BCrypt hash)
		if (!passwordEncoder.matches(password, dbPassword)) {
			throw new BusinessException("帳號或密碼錯誤");
		}
		return dbMember;
	}

	// 查看當前會員資料
	@Transactional
	@Override
	public Member profile(Member member) {
		if (member == null || member.getEmail() == null || member.getEmail().isBlank()) {
			throw new BusinessException("尚未登入");
		}
		String email = member.getEmail();
		Member dbMember = memberDao.selectByEmail(email);
		if (dbMember == null) {
			throw new BusinessException("找不到會員資料");
		}
		return dbMember;
	}

	// 更新會員資料
	@Transactional
	@Override
	public Member updateProfile(Member member, UpdateMemberRequest dto) {
		if (member == null) {
			throw new BusinessException("尚未登入");
		}
		// 1:撈出資料庫舊資料
		String email = member.getEmail();
		Member dbmember = memberDao.selectByEmail(email);
		if (dbmember == null) {
			throw new BusinessException("找不到該會員資料");
		}
		// 2:部分更新邏輯，只有傳值進來且不為空時才驗證並更新
		if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
			dbmember.setName(dto.getName());
		}
		if (dto.getAddress() != null) {
			dbmember.setAddress(validateAddress(dto.getAddress()));
		}
		
		String newPhone = dto.getPhone();
		//判斷：「使用者這次有沒有要改電話？」
		if (newPhone != null && !newPhone.isBlank()) {
			//只有跟舊手機不同時，驗證格式與重複
			if (!newPhone.equals(dbmember.getPhone())) {
				validatePhone(newPhone);
			}
			//檢查電話唯一性
			//如果查得到人，且那個人「不是我本人」(Email 不一樣)，才會衝突
			Member phoneOwner = memberDao.selectByPhone(dto.getPhone());
			if (phoneOwner != null && !phoneOwner.getEmail().equals(dbmember.getEmail())) {
				throw new BusinessException("手機號碼已被其他用戶使用!");
			}
		}
		dbmember.setPhone(newPhone);

		return dbmember;
	}

	// 修改密碼
	@Transactional
	@Override
	public void changePassword(String email, String oldPassword, String newPassword,String confirmPassword) {

		Member memberDb = memberDao.selectByEmail(email);
		if (memberDb == null) {
			throw new BusinessException("會員帳號不存在");
		}
		
		// 舊密碼驗證
		String dbPassword = memberDb.getPassword();
		if (!passwordEncoder.matches(oldPassword, dbPassword)) {
			throw new BusinessException("舊密碼錯誤");
		}
		if (passwordEncoder.matches(newPassword, memberDb.getPassword())) {
	        throw new BusinessException("新密碼不得與舊密碼相同");
	    }
		// 驗證新密碼格式與
		validatePassword(newPassword, confirmPassword);
		
		String newPasswordHash = passwordEncoder.encode(newPassword);
		memberDb.setPassword(newPasswordHash);
		memberDao.update(memberDb);

	}

	// 刪除會員(停用)
	@Transactional
	@Override
	public void remove(String email,String password ) {
		//1. 撈出會員
	    Member dbMember = memberDao.selectByEmail(email);
	    if (dbMember == null) {
	        throw new BusinessException("帳號不存在");
	    }
		
	    //2. 驗證密碼：確保是本人操作
	    if (!passwordEncoder.matches(password, dbMember.getPassword())) {
	        throw new BusinessException("密碼錯誤，無法刪除帳號");
	    }
		//3.改成停用狀態
	    int result = memberDao.updateStatusByEmail(0, email);
	    
	    if (result == 0) {
	    	throw new BusinessException("帳號註銷失敗，請稍後再試");
		}
	}

	// 查看訂單
	@Transactional
	@Override
	public PageResultResponse<Orders> viewMyOrder(Member member, int page, int size) {
		if (member == null || member.getMemberId() == null) {
			throw new BusinessException("會員ID不可為空");
		}
		if (page <= 0 || size <= 0) {
			throw new BusinessException("頁碼或每頁數量不正確");
		}
		int memberId = member.getMemberId();
		// 取得分頁
		int offset = (page - 1) * size;
		// 當頁訂單資料
		List<Orders> orders = memberDao.selectAllOrdersWithPagination(memberId, offset, size);
		// 總訂單數
		long totalOrders = memberDao.countAllOrdersById(memberId);
		// 總頁數
		int totalPages = (int) Math.ceil((float) totalOrders / size);
		return new PageResultResponse<>(orders, totalOrders, totalPages, page);

	}

	@Transactional
	@Override
	public List<CartItemResponse> viewMyCartItem(Member member) {
		if (member == null || member.getMemberId() == null) {
			throw new BusinessException("找不到該會員!");
		}
		List<Object[]> items = memberDao.selectAllCartItems(member.getMemberId());
		List<CartItemResponse> result = items.stream().map(obj -> {
			CartItem cartItem = (CartItem) obj[0];
			Product product = (Product) obj[1];
			return new CartItemResponse(cartItem, product);
		}).collect(Collectors.toList());

//		BeanUtils.copyProperties(items, result);

		return result;
	}

}
