package web.member.dao.impl;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.member.dao.PasswordResetTokensDao;
import web.member.vo.PasswordResetTokens;

@Repository
public class PasswordResetTokensDaoImpl implements PasswordResetTokensDao {
	@PersistenceContext
	private Session session;

	// 建立token
	@Override
	public void insert(PasswordResetTokens token) {
		session.persist(token);
	}

	// 查token
	@Override
	public PasswordResetTokens findByToken(String token) {

		PasswordResetTokens result = session
				.createQuery("FROM PasswordResetTokens WHERE token = :token", PasswordResetTokens.class)
				.setParameter("token", token).uniqueResult();
		return result;
	}

	// 存已用過的token進去資料庫
	@Override
	public void update(PasswordResetTokens token) {

		session.update(token);
	}

	@Override
	public void invalidateOldTokens(Integer memberId) {
		final String hql = "UPDATE PasswordResetTokens p SET p.used = :used "
				+ "WHERE p.memberId = :memberId AND p.used = false";
		session.createQuery(hql)
		.setParameter("used", true)
		.setParameter("memberId", memberId)
		.executeUpdate();

	}

}
