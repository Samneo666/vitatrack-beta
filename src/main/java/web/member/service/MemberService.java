package web.member.service;

import web.member.vo.Member;
import web.member_admin.dto.PageResultResponse;

import java.util.List;

import web.checkout.vo.Orders;
import web.member.dto.CartItemResponse;
import web.member.dto.UpdateMemberRequest;
import core.exception.BusinessException;

public interface MemberService {

	default void validateName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException("會員名為必填欄位!");
		}
	}

	default void validateEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new BusinessException("Email為必填欄位!");
		}
		if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			throw new BusinessException("Email格式錯誤!(請輸入有效的 Email，例如：example@mail.com)");
		}
	}

	default void validatePhone(String phone) {
		if (phone == null || phone.isBlank()) {
			throw new BusinessException("手機號碼未填寫!");
		}
		if (!phone.matches("^09[0-9]{8}$")) {
			throw new BusinessException("手機號碼格式錯誤!(必須是09開頭且共10位數字)");
		}
	}

	default void validatePassword(String password, String confirmPassword) {
		if (password == null || password.isBlank()) {
			throw new BusinessException("密碼未填寫!");
		}
		if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {

			throw new BusinessException("密碼格式錯誤(需至少8字元，含英文字母與數字)");
		}
		if (!password.equals(confirmPassword)) {
			throw new BusinessException("與設定密碼不一致，請重新輸入!");
		}
	}

	default String validateAddress(String address) {
		if (address == null || address.isBlank()) {
			return null;
		}
		address = address.trim();
		if (address.length() > 200) {
			throw new BusinessException("地址長度過長");
		}
		//簡易防XSS
		return address.replaceAll("<[^>]*>", "");
	}

	String register(Member member);

	Member login(Member member);

	Member profile(Member member);

	public Member updateProfile(Member member, UpdateMemberRequest dto);

	void changePassword(String email, String oldPassword, String newPassword,String confirmPassword);

	void remove(String email, String password);

	PageResultResponse<Orders> viewMyOrder(Member member, int page, int size);

	List<CartItemResponse> viewMyCartItem(Member member);
}
