package web.member.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data 
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberRequest {
	private String name;
	private String phone;
	private String address;
}
