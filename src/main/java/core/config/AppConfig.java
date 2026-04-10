package core.config;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.MySQLDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.orm.hibernate5.SpringSessionContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"web.*.*.impl"," web.*.util"})
@EnableTransactionManagement
@Import(MailConfig.class)
public class AppConfig {
	
	@Bean
	public DataSource dataSource() throws IllegalArgumentException, NamingException {
		
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setResourceRef(true);
		bean.setJndiName("jdbc/vitatrack");
		bean.afterPropertiesSet();
		return (DataSource) bean.getObject();
	}
	
	
	@Bean
	public SessionFactory sessionFactory() throws IllegalArgumentException, NamingException {
	
	return new LocalSessionFactoryBuilder(dataSource())
	.scanPackages("web.*.vo")
	.addProperties(getHibernateProperties())
	.buildSessionFactory();
	}
	
	private Properties getHibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", MySQLDialect.class.getName());
		properties.setProperty("hibernate.show_sql", "true");
		properties.setProperty("hibernate.format_sql", "true");
		properties.setProperty(
		"hibernate.current_session_context_class",
		SpringSessionContext.class.getName());
		return properties;
		}
	
	@Bean
	public TransactionManager transactionManager() throws IllegalArgumentException, 
	NamingException {
	return new HibernateTransactionManager(sessionFactory());
	}
	
	@Bean
	 public PasswordEncoder passwordEncoder() {
		 return new BCryptPasswordEncoder(12); //長度12常見安全值
	}
}
