package web.member_admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.member.dao.AdminDao;
import web.member.dao.MemberDao;
import web.member.dto.EditMemberStatusRequest;
import web.member.dto.EditMemberStatusResponse;
import core.exception.BusinessException;
import web.member.vo.Admin;
import web.member.vo.Member;
import web.member_admin.dto.MemberListResponse;
import web.member_admin.dto.PageResultResponse;
import web.member_admin.service.MemberAdminService;

@Service
public class MemberAdminServiceImpl implements MemberAdminService {
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	@Override
	public PageResultResponse<MemberListResponse> getMemberInfo(int page, int size) {
		if (page < 1 || size < 1) {
			throw new BusinessException("分頁參數錯誤");
		}
		// 計算分頁起始位置。
		int offset = (page - 1) * size;
		List<MemberListResponse> members = memberDao.selectAllWithPagination(offset, size);

		// 計算總會員數
		long totalNumber = memberDao.countAllMembers();
		// 計算共需要幾頁
		int totalPages = (int) Math.ceil((double) totalNumber / size);

		return new PageResultResponse<>(members, totalNumber, totalPages, page);

	}

	@Transactional(readOnly = true)
	@Override
	public PageResultResponse<MemberListResponse> searchMemberInfo(String keyword, int page, int size) {
		keyword = keyword.trim();
		if (keyword == null || keyword.isEmpty()) {
			return getMemberInfo(page, size);
		}
		if (page < 1 || size < 1) {
			throw new BusinessException("分頁參數錯誤");
		}

		// 取得分頁
		int offset = (page - 1) * size;
		List<MemberListResponse> members = memberDao.searchMemberWithPagination(keyword, offset, size);
		// 計算總會員數
		long totalNumber = memberDao.countMemberByKeyword(keyword);
		// 計算總頁數
		int totalPages = (int) Math.ceil((double) totalNumber / size);

		return new PageResultResponse<>(members, totalNumber, totalPages, page);

	}

	@Transactional
	@Override
	public Admin login(Admin admin) {

		String account = admin.getAccount();
		String password = admin.getPassword();
		if (account == null || account.isEmpty() || password == null || password.isEmpty()) {
			throw new BusinessException("帳號或密碼不可為空");
		}

		Admin dbAdmin = adminDao.selectByAccount(account);

		if (dbAdmin == null) {
			throw new BusinessException("此帳號不存在!");
		}
		String dbPassword = dbAdmin.getPassword();
		if (!passwordEncoder.matches(password,dbPassword)) {

			throw new BusinessException("輸入的帳號或密碼不正確");
		}
		
		return dbAdmin;
	}

	@Transactional
	@Override
	public EditMemberStatusResponse editMemberStatus(Admin admin, EditMemberStatusRequest member) {
		if (admin == null) {
			throw new BusinessException("管理員尚未登入");
		}
		if (!"SUPER_ADMIN".equals(admin.getRole())) {
			throw new BusinessException("您沒有權限執行此操作");
		}
		if (member == null || member.getEmail() == null || member.getMemberStatus() == null) {
			throw new BusinessException("更新失敗：Email或會員狀態不能為空！");
		}

		String email = member.getEmail().trim();
		Integer newStatus = member.getMemberStatus();
		if (newStatus != 0 && newStatus != 1) {
			throw new BusinessException("會員狀態值不合法");
		}

		Member dbMember = memberDao.selectByEmail(email);
		if (dbMember == null) {
			throw new BusinessException("會員不存在");
		}

		Integer count = memberDao.updateStatusByEmail(member.getMemberStatus(), member.getEmail());
		if (count == 0) {
			throw new BusinessException("會員狀態更新失敗");
		}

		return new EditMemberStatusResponse(member.getEmail(), member.getMemberStatus());
	}

}
