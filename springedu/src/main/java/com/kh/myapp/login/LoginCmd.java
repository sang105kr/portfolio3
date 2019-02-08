package com.kh.myapp.login;

import javax.persistence.Entity;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Data
public class LoginCmd {
	
	@Pattern(regexp="^\\w+@\\w+\\.\\w+(\\.\\w+)?$", message="ex)aaa@bbb.com")	
	private String id; //아이디
	@Size(min=4,max=30, message="비밀번호는 4~30자리로 입력바랍니다.")	
	private String pw; //비밀번호
	
}
