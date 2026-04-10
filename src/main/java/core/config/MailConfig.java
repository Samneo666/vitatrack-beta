package core.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@PropertySource("classpath:mail.properties") // 自動加載檔案
public class MailConfig {

    @Value("${mail.host}")
    private String host;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;
    

    //負責"設定連線並連線到SMTP伺服器"和"把信寄出去"
    @Bean   
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");// 啟用TLS安全加密
        props.put("mail.debug", "true");         // 測試時開啟，可以看到發信過程的log
        props.put("mail.smtp.connectiontimeout", "5000"); // 建立連線逾時5秒
        props.put("mail.smtp.timeout", "5000");           // 讀取資料逾時5秒
        props.put("mail.smtp.writetimeout", "5000");	  // 寫入資料逾時5秒(為了防止因為網路問題導致程式卡死（例如伺服器連不上SMTP）)
        return mailSender;
    }
}