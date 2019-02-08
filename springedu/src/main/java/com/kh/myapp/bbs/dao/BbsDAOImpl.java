package com.kh.myapp.bbs.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import com.kh.myapp.bbs.dto.BbsDTO;

//@Repository
public class BbsDAOImpl implements BbsDAO {

	private static Logger logger = LoggerFactory.getLogger(BbsDAOImpl.class);
	
	@Inject
	JdbcTemplate jdbcTemplate;
	
	// 글쓰기
	@Override
	public int write(BbsDTO bbsDTO) throws Exception {

		logger.info("void write(BbsDTO bbsDTO) 호출됨: "+bbsDTO);
		
		int cnt = 0;
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO board (bnum,btitle,bid,bnickname,bhit,bcontent,bgroup,bstep,bindent) ");
		sql.append("values(boardnum_seq.nextval,?,?,?,0,?,boardnum_seq.currval,0,0) ");
		
		cnt = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, bbsDTO.getBtitle());
				ps.setString(2, bbsDTO.getBid());
				ps.setString(3, bbsDTO.getBnickname());
				ps.setString(4, bbsDTO.getBcontent());				
			}
		});
		
		if(cnt>0) {
			logger.info("등록건수 : "+ cnt);
		}else {
			logger.info("등록건수 : "+ cnt);
		}
		
		return cnt;
	}
	// 글목록
	@Override
	public List<BbsDTO> list() throws Exception {
		
		logger.info("List<BbsDTO> list()");	
		
		List<BbsDTO> list = null;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT bnum,btitle,bid,bnickname,bcdate,budate, ");
		sql.append("	     bhit,bcontent,bgroup,bstep,bindent ");
		sql.append("  FROM (select * from board order by bgroup desc, bstep asc) ");		
		sql.append(" where rownum >=1 and rownum < 25 ");		
		
		list = (ArrayList<BbsDTO>)jdbcTemplate.query(sql.toString(),new BeanPropertyRowMapper<>(BbsDTO.class));
		
		return list;
	}

	@Override
	public List<BbsDTO> list(int starRec, int endRec) throws Exception {
		
		logger.info("ArrayList<BbsDTO> list(int starRec, int endRec)");	
		
		List<BbsDTO> list = null;
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select t2.* ");
		sql.append("from (select row_number() over (order by bgroup desc, bstep asc) as num,t1.* ");
		sql.append("		    from board t1 ) t2 ");
		sql.append("where num between ? and ? ");
		
		list = (ArrayList<BbsDTO>)jdbcTemplate.query(
				sql.toString(),
				new Object[] {starRec,endRec},
				new BeanPropertyRowMapper<BbsDTO>(BbsDTO.class));
		
		return list;
	}
	// 글읽기
	@Override
	public BbsDTO view(String bnum) throws Exception {
		
		logger.info("BbsDTO view(String bnum) 호출됨!");
		
		BbsDTO bbsdto = null;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT bnum,btitle,bid,bnickname,bcdate,budate, ");
		sql.append("	     bhit,bcontent,bgroup,bstep,bindent ");
		sql.append("  FROM board ");
		sql.append(" where bnum=? ");

		bbsdto = jdbcTemplate.queryForObject(
				sql.toString(), 
				new Object[] {bnum}, 
				new BeanPropertyRowMapper<BbsDTO>(BbsDTO.class));
		
		// 조회수증가
		updateHit(bbsdto.getBnum());
		
		return bbsdto;
	}
	
	private void updateHit(int bnum) {
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("update board set bhit = bhit + 1 ");
		sql.append("where bnum=? ");

//		cnt = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
//			@Override
//			public void setValues(PreparedStatement ps) throws SQLException {
//				ps.setInt(1, bnum);
//			}
//		});
		//람다식 적용!
		cnt = jdbcTemplate.update(sql.toString(), ps-> {ps.setInt(1, bnum);});
		
		if(cnt>0) {
			logger.info("조회건수증가 : "+ cnt);
		}else {
			logger.info("조회건수증가 : "+ cnt);
		}				
	}
	// 글수정
	@Override
	public int modify(BbsDTO boardDTO) throws Exception {
		
		logger.info("modify(BbsDTO boardDTO) 호출됨!");
		
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("update board set btitle=?,budate=sysdate, bcontent=? ");
		sql.append("where bnum=? ");

		cnt = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, boardDTO.getBtitle());	
				ps.setString(2, boardDTO.getBcontent());	
				ps.setInt(3, boardDTO.getBnum());					
			}
		});
		
		if(cnt>0) {
			logger.info("수정건수 : "+ cnt);
		}else {
			logger.info("수정건수 : "+ cnt);
		}		
		
		return cnt;
	}
	// 글삭제
	@Override
	public int delete(String bnum) throws Exception {
		logger.info("int delete(String bnum) 호출됨!");
		int cnt = 0;
		StringBuffer sql = new StringBuffer();
		
		// 답글존재유무 판단
		if(isReply(bnum)) {
			//답글 존재
			sql.append("update board set isdel = 'Y' where bnum = ?");
			cnt = jdbcTemplate.update(sql.toString(),bnum);
					
		}else{
			//답글 미존재
			sql.append("delete from board where bnum=?");
			cnt = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
						ps.setInt(1, Integer.valueOf(bnum));
				}
			});
		}
		
		if(cnt>0) {
			logger.info("삭제건수 : "+ cnt);
		}else {
			logger.info("삭제건수 : "+ cnt);
		}			
		
		return cnt;
	}

	//원글에 대한 답글이 존재하는지 판단.
	private boolean isReply(String bnum) {
		boolean isYN = false;
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(bnum) from board "); 
		sql.append("where bgroup in ( select bgroup from board t1 "); 
		sql.append("                                 where t1.bnum = ?) "); 
		sql.append("  and bnum <> ?"   );
		
		cnt = jdbcTemplate.queryForObject(//sql문,파라미터,리턴타입
				sql.toString(),	new Object[] {bnum, bnum}, Integer.class);
		if(cnt > 0) {
			isYN = true;
		}
		return isYN;
	}
	
	// 원글가져오기
	@Override
	public BbsDTO replyView(String bnum) throws Exception {
		
		logger.info("BbsDTO replyView(String bnum) 호출됨!");		
		
		BbsDTO bbsdto = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT bnum,btitle,bid,bnickname,bcdate,budate, ");
		sql.append("	     bhit,bcontent,bgroup,bstep,bindent ");
		sql.append("  FROM board ");
		sql.append(" where bnum=? ");
				
		bbsdto = jdbcTemplate.queryForObject(
				sql.toString(),
				new Object[] {bnum},
				new BeanPropertyRowMapper<BbsDTO>(BbsDTO.class));
		
		return bbsdto;
	}
	// 답글쓰기
	@Override
	public int reply(BbsDTO bbsDTO) throws Exception {
		logger.info("void reply(BbsDTO bbsDTO) 호출됨!");	
		int cnt1=0 , cnt2=0;
		
		//이전 답글 step 업데이트(원글그룹에 대한 세로정렬 재정의)
		cnt1 = updateStep(bbsDTO.getBgroup(), bbsDTO.getBstep());
		
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO board (bnum,btitle,bid,bnickname,bhit,bcontent,bgroup,bstep,bindent) ");
		sql.append("values(boardnum_seq.nextval,?,?,?,0,?,?,?,?) ");
		
		cnt2 = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, bbsDTO.getBtitle());
				ps.setString(2, bbsDTO.getBid());
				ps.setString(3, bbsDTO.getBnickname());
				ps.setString(4, bbsDTO.getBcontent());
				ps.setInt(5, bbsDTO.getBgroup());					//원글번호 = 원글 그룹
				ps.setInt(6, bbsDTO.getBstep()+1);         //원글 그룹의 세로정렬(답글단계)
				ps.setInt(7, bbsDTO.getBindent()+1);				//원글 그룹의 가로정렬(들여쓰기)

			}
		});
		
		if(cnt2>0) {
			logger.info("답글게제건수 : "+ cnt2);
		}else {
			logger.info("답글게제건수 : "+ cnt2);
		}	
		return cnt2;
	}
	
	private int updateStep(int bgroup, int bstep) {
		logger.info("int updateStep(int bgroup, int bstep) 호출됨!!");
		
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("update board set bstep=bstep+1 where bgroup=? and bstep>?");
		
		cnt = jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, bgroup);
				ps.setInt(2, bstep);	
			}
		});
		
		if(cnt>0) {
			logger.info("게시글그룹/게시글스텝 1증가 : "+ cnt);
		}else {
			logger.info("게시글그룹/게시글스텝 1증가 : "+ cnt);
		}	
		
		return cnt;
	}
	
	// 게시글 총계
	@Override
	public int totalRec() throws Exception {
		logger.info("int totalRec() 호출됨!");
		int totalRec = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT count(*) totalRec from board ");
		
		totalRec = (Integer)jdbcTemplate.queryForObject(sql.toString(), Integer.class);
		
		return totalRec;
	}
	// 검색목록
	@Override
	public List<BbsDTO> list(int startRecord, int endRecord, String searchType, String keyword) throws Exception {
		logger.info("ArrayList<BbsDTO> list(int startRecord, int endRecord, String searchType, String keyword) 호출됨!");
		List<BbsDTO> list = null;
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select t2.* ");
		sql.append("from (select row_number() over (order by bgroup desc, bstep asc) as num,t1.* ");
		sql.append("		    from board t1 ");
		sql.append("			 where bnum > 0	");
		
		switch(searchType){
		case "TC": // 제목 + 내용
			sql.append("and btitle like '%' || ? || '%' or bcontent like '%' || ? || '%' ");		
			break;
		case "T": // 제목
			sql.append("and btitle like '%' || ? || '%' ");			
			break;
		case "C": // 내용
			sql.append("and bcontent like '%' || ? || '%' ");			
			break;
		case "N": // 작성자
			sql.append("and bnickname like '%' || ? || '%' ");			
			break;
		case "I": // 아이디
			sql.append("and bid like '%' || ? || '%' ");
			break;
		default:  // 제목 + 내용 + 작성자
			sql.append("and btitle like '%' || ? || '%' or bcontent like '%' || ? || '%' or bnickname like '%' || ? || '%' ");
			break;
		}
		sql.append("		 ) t2 ");
		sql.append("where num between ? and ? ");
		
		Object[] obj = null; //jdbcTemplate.query메소드의 2번째인자 변수 
		switch(searchType) {
		case "TC":
			obj = new Object[] {keyword, keyword,startRecord,endRecord};				
			break;
		case "T":
		case "C":
		case "N":
		case "I":
			obj = new Object[] {keyword, startRecord,endRecord};
			break;
		default:
			obj = new Object[] {keyword, keyword, keyword, startRecord,endRecord};
			break;
		}
		
		list = (ArrayList<BbsDTO>)jdbcTemplate.query(
				sql.toString(), 
				obj,
				new BeanPropertyRowMapper<BbsDTO>(BbsDTO.class)
		);		
		
		return list;
	}
	
	// 검색 총계
	@Override
	public int SearchTotalRec(String searchType, String keyword) throws Exception {
		logger.info("int SearchTotalRec(String searchType, String keyword)");
		
		int totalRec = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) totalRec ");
		sql.append("from (select row_number() over (order by bgroup desc, bstep asc) as num,t1.* ");
		sql.append("		    from board t1 ");
		sql.append("			 where bnum > 0	");
		
		switch(searchType){
		case "TC": // 제목 + 내용
			sql.append("and btitle like '%' || ? || '%' or bcontent like '%' || ? || '%' ");		
			break;
		case "T": // 제목
			sql.append("and btitle like '%' || ? || '%' ");			
			break;
		case "C": // 내용
			sql.append("and bcontent like '%' || ? || '%' ");			
			break;
		case "N": // 작성자
			sql.append("and bnickname like '%' || ? || '%' ");			
			break;
		case "I": // 아이디
			sql.append("and bid like '%' || ? || '%' ");
			break;
		default:  // 제목 + 내용 + 작성자
			sql.append("and btitle like '%' || ? || '%' or bcontent like '%' || ? || '%' or bnickname like '%' || ? || '%' ");
			break;
		}		
		sql.append("		 ) t2 ");	
		
		Object[] obj = null;
		switch(searchType) {
		case "TC":
			obj = new Object[] {keyword,keyword};
			break;
		case "T":
		case "C":
		case "N":
		case "I":
			obj = new Object[] {keyword};						
			break;
		default:
			obj = new Object[] {keyword,keyword,keyword};
			break;
		}		
		
		totalRec = (Integer)jdbcTemplate.queryForObject(
				sql.toString(), obj, Integer.class);
		
		return totalRec;
	}
}
