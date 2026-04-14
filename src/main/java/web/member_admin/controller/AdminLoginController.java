package web.member_admin.controller;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;
import core.exception.BusinessException;
import web.member.vo.Admin;

import web.member_admin.service.MemberAdminService;

@RestController
public class AdminLoginController {
	@Autowired
	private MemberAdminService memberAdminService;
	
	@PostMapping("/api/adminLogin")
	public ApiResponse<Void> adminLogin(@RequestBody Admin admin, HttpServletRequest request) {
		
		Admin loginAdmin = memberAdminService.login(admin);
		
		//資安考量
		if (request.getSession(false) != null) {
			request.changeSessionId();
		}
		if (loginAdmin == null) {
			throw new BusinessException("帳號或密碼錯誤，請重新登入!");
		}
		HttpSession session = request.getSession();
		session.setAttribute("admin", loginAdmin);
		return new ApiResponse<Void>( true,"登入成功!",null);
	}

}
