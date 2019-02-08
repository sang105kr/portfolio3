package com.kh.myapp.member.dto;


import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Data
public class MemberDTO {
  
	@Pattern(regexp="^\\w+@\\w+\\.\\w+(\\.\\w+)?$", message="ex)aaa@bbb.com")
	private String id; 				//회원아이디
	@Size(min=4,max=30, message="비밀번호는 4~30자리로 입력바랍니다.")
	private String pw; 				//비밀번호
	@Pattern(regexp="^(02|010)-\\d{3,4}-\\d{4}$",message="ex)010-1234-5678")
	private String tel; 			//전화번호
	@Size(min=4,max=10, message="닉네임은 3~10자리로 입력바랍니다.")
	private String nickName; 	//닉네임
	private String gender;  	//성별
	private String region; 		//지역
	@Pattern(regexp="^\\d{4}-\\d{2}-\\d{2}$", message="생년월일 ex)xxxx-xx-xx")
	private String birth; 		//생년월일
	private Timestamp cdate;				//가입일
	private Timestamp udate;				//변경일
}
