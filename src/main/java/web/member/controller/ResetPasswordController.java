package web.member.controller;


import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import web.member.service.PasswordResetTokensService;
import web.member.dto.ResetPasswordRequest;


@RestController
public class ResetPasswordController  {
	@Autowired
	private PasswordResetTokensService passwordResetTokensService;
	//執行「重設密碼」
	@PostMapping("/api/resetPassword")
	public  Map<String, Object> resetPassword(@RequestBody ResetPasswordRequest dto) {
		
		passwordResetTokensService.resetPassword(dto.getToken(),dto.getNewPassword());
		return Map.of("success",true,"message", "密碼重設成功");
	}
}
