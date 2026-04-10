package web.member.vo;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



import lombok.Data;

@Entity
@Table(name = "password_reset_tokens")
@Data
public class PasswordResetTokens {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_id")
	private Integer tokenId;
	@Column(name="member_id")
	private Integer memberId;
	@Column(name = "token")
	private String token;
	@Column(name ="create_time")
	private Timestamp createTime;
	@Column(name = "used")
	private Boolean used;
	@Column(name ="expiry_time")
	private Timestamp expiryTime;
}
