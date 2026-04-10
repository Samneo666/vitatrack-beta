package web.member.dao;

import web.member.vo.PasswordResetTokens;

public interface PasswordResetTokensDao {
	void insert(PasswordResetTokens token);

	PasswordResetTokens findByToken(String token);

	void update(PasswordResetTokens token);

	void invalidateOldTokens(Integer memberId);
}