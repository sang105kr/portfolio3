package com.kh.myapp.board;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.kh.myapp.bbs.dao.RbbsDAO;
import com.kh.myapp.bbs.dto.RbbsDTO;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations= {"file:src/main/webapp/WEB-INF/spring/**/*.xml"})
public class RboardTest {

	private Logger logger = LoggerFactory.getLogger("RboardTest.class");
	
	@Inject
	@Qualifier("rbbsDAOImplXML")
	RbbsDAO rbbsDAO;
	
	RbbsDTO rbbsDTO;
	int cnt;
	
	//댓글 등록
	@Test @Disabled
	void insert() {
		rbbsDTO = new RbbsDTO();
		rbbsDTO.setBnum(10203);
		rbbsDTO.setRid("test2@test.com");
		rbbsDTO.setRnickname("테스터2");
		rbbsDTO.setRcontent("10203글에 대한 댓글");
		
		try {
		 cnt = rbbsDAO.write(rbbsDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("처리건수 : " + cnt);
	}
		
	//댓글 수정
	@Test @Disabled
	void update() {
		rbbsDTO = new RbbsDTO();
		rbbsDTO.setRcontent("[댓글수정]10203글에 대한 댓글");
		rbbsDTO.setRnum(261);
		try {
		 cnt = rbbsDAO.modify(rbbsDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("처리건수 : " + cnt);
	}
	
	//댓글 삭제
	@Test @Disabled
	void delete() {
		
		try {
			cnt = rbbsDAO.delete("256");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("처리건수 : " + cnt);
	}
	
	//댓글 호감 비호감
	@Test @Disabled
	void goodOrBad() {
		try {
			cnt = rbbsDAO.goodOrBad("261", "good");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("처리건수 : " + cnt);
	}
	
	//대댓글등록
	@Test @Disabled
	void reply() {
		int cnt = 0;
		RbbsDTO rbbsDTO = new RbbsDTO();
		rbbsDTO.setRnum(258); //원글번호
		rbbsDTO.setBnum(10203); //최초등록글
		rbbsDTO.setRid("test2@test.com");
		rbbsDTO.setRnickname("테스터2");
		rbbsDTO.setRcontent("대댓글 테스트");
		rbbsDTO.setRgroup(258); //원글번호 = 원글 그룹
		rbbsDTO.setRstep(0+1); //원글 그룹의 세로정렬(답글단계)
		rbbsDTO.setRindent(0+1); //원글 그룹의 가로정렬(들여쓰기)
		rbbsDTO.setRrnum(258);
		try {
			cnt = rbbsDAO.reply(rbbsDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("처리건수 : " + cnt);
	}
	
	//목록가져오기
	@Test @Disabled
	void list() {

		List<RbbsDTO> list = null;
		try {
			list = rbbsDAO.list("10172");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(list.toString());
	}
	
	//목록가져오기
	@Test @Disabled
	void list2() {

		List<RbbsDTO> list = null;
		try {
			list = rbbsDAO.list("10063",1,10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(list.toString());
	}
	//목록가져오기
	@Test @Disabled
	void list3() {
		
		List<RbbsDTO> list = null;
		try {
			logger.info("ggg");
			list = rbbsDAO.list("10203",1,10);
			logger.info("hhh");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(list.toString());
	}

	//총계
	@Test 
	void totalCnt() {
		try {
			cnt = rbbsDAO.replyTotalRec("10203");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("총계:"+cnt);
	}
}




