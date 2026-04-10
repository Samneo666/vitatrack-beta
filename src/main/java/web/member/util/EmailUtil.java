package web.member.util;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


//組合成重設密碼的URL
@Component
public class EmailUtil {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${app.base.url}") // 注入 http://localhost:8080
    private String baseUrl;
	
	public void sendResetPasswordEmail(String email, String token) {
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
        message.setSubject("VitaTrack重設密碼通知");
        
        String resetUrl = baseUrl+"/resetPassword.html?token=" + token;
        message.setText("您好：\n\n請點擊以下連結以重設您的密碼（連結於15分鐘內有效）：\n" + resetUrl 
                + "\n\n如果這不是您本人的操作，請忽略此郵件。");
        
        mailSender.send(message);
	}
	
	public void sendPasswordChangedNotification(String email) {
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("【VitaTrack】帳號安全通知：密碼已變更");
		String currentTime = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(java.time.LocalDateTime.now());
		
		message.setText("您好：\n\n您的密碼已於 " + currentTime + " 變更成功。\n" +
                "如果這是您本人所做的變更，則無需採取任何行動。\n\n" +
                "如果您並未要求變更密碼，請立即連繫我們的客服團隊。");
		
		mailSender.send(message);
		
	}

}