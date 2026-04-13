package web.member.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;
import web.member.service.PasswordResetTokensService;
import web.member.dto.ForgetPasswordRequest;


@RestController
public class ForgetPasswordController  {

	@Autowired
	private PasswordResetTokensService passwordResetTokensService;

	@PostMapping("/api/forgetPassword")
	public ApiResponse<Void> forgetPassword(@RequestBody ForgetPasswordRequest DTO) {
		passwordResetTokensService.createResetToken(DTO.getEmail());
		return new ApiResponse<>(true, "已寄出密碼重設連結", null);
	}

}
