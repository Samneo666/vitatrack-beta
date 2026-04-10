package web.member.service;


import web.member.vo.PasswordResetTokens;


public interface PasswordResetTokensService {

    void createResetToken(String email);
    
    PasswordResetTokens validateToken(String token);
    
    void resetPassword(String token, String newPassword);
  
}
