package web.member.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.member.dao.MemberDao;
import web.member.dao.PasswordResetTokensDao;
import core.exception.BusinessException;
import web.member.service.PasswordResetTokensService;
import web.member.util.EmailUtil;
import web.member.vo.Member;
import web.member.vo.PasswordResetTokens;

@Service
public class PasswordResetTokensServiceImpl implements PasswordResetTokensService {
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PasswordResetTokensDao passwordResetTokenDao;
	
	@Autowired
	private EmailUtil emailUtil;

	private static final Logger logger = LogManager.getLogger(PasswordResetTokensServiceImpl.class);
	
	//建立token
	@Transactional
	@Override
	public void createResetToken(String email) {
		
		String token = null;

		Member member = memberDao.selectByEmail(email);
		if (member == null) {
			logger.warn("有人嘗試用不存在的帳號建立token",email);
			throw new BusinessException("此帳號不存在");
		}
		//將舊的未使用的Token設為已用過(防止一堆有效Token)
		passwordResetTokenDao.invalidateOldTokens(member.getMemberId());
		//建立新 Token
		token = UUID.randomUUID().toString();
		PasswordResetTokens resetToken = new PasswordResetTokens();
		resetToken.setMemberId(member.getMemberId());
		resetToken.setToken(token);
		resetToken.setUsed(false);
		LocalDateTime now = LocalDateTime.now();
		resetToken.setCreateTime(Timestamp.valueOf(now));
		resetToken.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
		
		passwordResetTokenDao.insert(resetToken);
		//寄信
		emailUtil.sendResetPasswordEmail(email, token);

	}

	// 確認這個Token是否合法、有效、可使用
	@Transactional(readOnly = true)
	@Override
	public PasswordResetTokens validateToken(String token) {
		// 避免後續DAO查詢時出現不必要錯誤或無效查詢
		if (token == null || token.trim().isEmpty()) {

			throw new BusinessException("Token不可為空");
		}
		PasswordResetTokens resetToken;
		resetToken = passwordResetTokenDao.findByToken(token);
		if (resetToken == null) {
			throw new BusinessException("Token無效或不存在");
		}
		// 為避免重複使用同一個重設連結，檢查token使否使用過
		if (resetToken.getUsed()) {
			throw new BusinessException("Token已使用過");
		}
		// 是否過期;System.currentTimeMillis():取得現在時間（毫秒）
		// before(now) 表示「是否早於現在時間」。若為 true，代表已過期。
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (resetToken.getExpiryTime().before(now)) {
			throw new BusinessException("Token已過期");
		}
		return resetToken;
	}

	@Transactional
	@Override
	public void resetPassword(String token, String newPassword) {
		
			// 驗證token
			PasswordResetTokens resetToken = validateToken(token);
			// 驗證密碼格式
			if (newPassword == null || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
				throw new BusinessException("密碼格式錯誤或未填寫(需至少8字元，含英文字母與數字)");
			}
			// 撈出會員資料;session.get() 從資料庫抓出對應的Member實體
			Member member = memberDao.selectByMemberId(resetToken.getMemberId());
			if (member == null) {
				throw new BusinessException("找不到對應會員帳號");
			}
			// 加密並更新會員密碼
			member.setPassword(passwordEncoder.encode(newPassword));
			memberDao.update(member);
			// 標記token已使用(token失效)
			resetToken.setUsed(true);
			passwordResetTokenDao.update(resetToken);		
			//安全機制
			emailUtil.sendPasswordChangedNotification(member.getEmail());
	}

}
