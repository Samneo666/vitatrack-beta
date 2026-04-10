package web.member_admin.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.exception.BusinessException;
import web.member.vo.Admin;
import web.member_admin.service.MemberAdminService;

@RestController
public class AdminLoginController {
	@Autowired
	private MemberAdminService memberAdminService;
	
	@PostMapping("/api/adminLogin")
	public Map<String, Object> adminLogin(@RequestBody Admin admin, HttpSession session) {
		Admin loginAdmin = memberAdminService.login(admin);
		if (loginAdmin == null) {
			throw new BusinessException("帳號或密碼錯誤，請重新登入!");
		}
		session.setAttribute("admin", loginAdmin);
		return Map.of("success", true, "message", "登入成功!");
	}

}
