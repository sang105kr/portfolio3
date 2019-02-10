package com.kh.myapp.member.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.kh.myapp.member.dao.MemberDAO;
import com.kh.myapp.member.dto.MemberDTO;

@Service
public class MemberSvcImpl implements MemberSvc {

	@Inject //동일타입의 인스턴스를 주입받는다.
	//@Qualifier 동일타입이 있을경우 매값에 구분자로 구현클래스명 또는 
	//ex) @Repository("name")인스턴스 이름을 지정할수있다.
	@Qualifier("memberDAOImplXML") 
	MemberDAO mdao;
	
	// 회원 등록
	@Override
	public boolean insert(MemberDTO memberDTO) {
		boolean success = false;
		success = mdao.insert(memberDTO);
		return success;
	}

	// 회원 수정
	@Override
	public boolean modify(MemberDTO memberDTO) {
		boolean success = false;
		
		// 파일첨부가 존재하면
		if(!memberDTO.getFile().isEmpty()) {	
		  if(!fileUpload(memberDTO)) {
		  	return false;
		  };	
		}
		
		success = mdao.modify(memberDTO);		  	
		return success;
	}

	// 파일 업로드
	private boolean fileUpload(MemberDTO memberDTO) {
		boolean isUpload = false;
		
		String randomFileName = null;   //난수 파일명
		String originFileName = null;   //초기 파일명
		String fileLocation = "D:\\LSH\\git\\repository\\springedu\\src\\main\\webapp\\resources\\upload";
			
		randomFileName = UUID.randomUUID().toString();
		originFileName = memberDTO.getFile().getOriginalFilename();
	
		// 초기 화일명에서 확장자 추출
		int pos = originFileName.lastIndexOf(".");
		String ext = originFileName.substring(pos+1);
		randomFileName = randomFileName + "." + ext;
		
		File tmpFile = new File(fileLocation, randomFileName);
		try {
			// 파일시스템에 파일쓰기
			memberDTO.getFile().transferTo(tmpFile);
			// memberDTO 갱신
			memberDTO.setFile(null);
			memberDTO.setOriginFileName(originFileName);
			memberDTO.setRandomFileName(randomFileName);
			isUpload = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isUpload;
	}

	//회원 삭제(회원용)
	@Override
	public boolean delete(String id, String pw) {
		boolean success = false;
		success = mdao.delete(id, pw);
		return success;

	}

	//회원 삭제(관리자용)
	@Override
	public boolean adminDelete(String id) {
		boolean success = false;
		success = mdao.adminDelete(id);
		return success;
	}
	
	// 회원 조회
	@Override
	public MemberDTO getMember(String id) {
		MemberDTO memberDTO = null;	
		
		memberDTO = mdao.getMember(id);
		return memberDTO;
	}

	// 회원 목록 조회
	@Override
	public List<MemberDTO> getMemberList() {
		List<MemberDTO> list = null;
		list = mdao.getMemberList();
		return list;
	}



}
