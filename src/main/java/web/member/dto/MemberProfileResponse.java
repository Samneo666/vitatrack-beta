package web.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.member.vo.Member;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberProfileResponse {
	private Integer memberId;
	private String name;
	private String email;
	private String address;
	private String phone;
	
	
	public MemberProfileResponse(Member member) {
        this.memberId = member.getMemberId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.phone = member.getPhone();
        this.address = member.getAddress();
    }

}
