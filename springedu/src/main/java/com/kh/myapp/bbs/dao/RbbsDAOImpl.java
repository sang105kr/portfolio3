package com.kh.myapp.bbs.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.kh.myapp.bbs.dto.RbbsDTO;

//@Repository(value="rbbsDAOImpl")
public class RbbsDAOImpl implements RbbsDAO {
	
	private Logger logger = LoggerFactory.getLogger("RbbsDAOImpl.class");
	
	@Inject
	JdbcTemplate jdbcTemplate;
	
	//댓글 등록
	@Override
	public int write(RbbsDTO rbbsDTO) throws Exception {
		int cnt = 0;
	
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO replyboard (rnum,bnum,rid,rnickname,rcontent,rgroup) ");
		sql.append("values(rboardnum_seq.nextval,?,?,?,?,rboardnum_seq.currval) ");
		
		cnt = jdbcTemplate.update(
				sql.toString(), 
				rbbsDTO.getBnum(),
				rbbsDTO.getRid(),
				rbbsDTO.getRnickname(),
				rbbsDTO.getRcontent()); 

		return cnt;
	}
	//댓글 목록
	@Override
	public List<RbbsDTO> list(String bnum) throws Exception {
		List<RbbsDTO> list = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT rnum,bnum,rid,rnickname,rcdate,rudate, ");
		sql.append("	     rcontent,rgood,rbad,rgroup,rstep,rindent,rrnum ");
		sql.append("  FROM (select * from replyboard ");
		sql.append("         where bnum = ? ");		
		sql.append("        order by rgroup desc, rstep asc ) ");		
		sql.append(" where rownum >=1 and rownum < 25 ");
		
		//익명클래스
		list = (List<RbbsDTO>)jdbcTemplate.query(
				sql.toString(), 
				new Object[] {bnum},
				new ResultSetExtractor <List<RbbsDTO>>() {

					@Override
					public List<RbbsDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {						
						List<RbbsDTO> list = new ArrayList<RbbsDTO>();
						while(rs.next()) {
							RbbsDTO rbbsDTO = new RbbsDTO();
							rbbsDTO.setRnum(rs.getInt("rnum"));
							rbbsDTO.setBnum(rs.getInt("bnum"));
							rbbsDTO.setRid(rs.getString("rid"));
							rbbsDTO.setRnickname(rs.getString("rnum"));
							rbbsDTO.setRcdate(rs.getTimestamp("rcdate"));
							rbbsDTO.setRudate(rs.getTimestamp("rudate"));
							rbbsDTO.setRcontent(rs.getString("rcontent"));
							rbbsDTO.setRgood(rs.getInt("rgood"));
							rbbsDTO.setRbad(rs.getInt("rbad"));
							rbbsDTO.setRgroup(rs.getInt("rgroup"));
							rbbsDTO.setRstep(rs.getInt("rstep"));
							rbbsDTO.setRindent(rs.getInt("rindent"));
							list.add(rbbsDTO);
						}	
						return list;
					}
				});
	
		//람다식
//		list = (List<RbbsDTO>)jdbcTemplate.query(
//				sql.toString(), 
//				new Object[] {bnum},
//				rs-> {						
//						List<RbbsDTO> alist = new ArrayList<RbbsDTO>();
//						while(rs.next()) {
//							RbbsDTO rbbsDTO = new RbbsDTO();
//							rbbsDTO.setRnum(rs.getInt("rnum"));
//							rbbsDTO.setBnum(rs.getInt("bnum"));
//							rbbsDTO.setRid(rs.getString("rid"));
//							rbbsDTO.setRnickname(rs.getString("rnum"));
//							rbbsDTO.setRcdate(rs.getTimestamp("rcdate"));
//							rbbsDTO.setRudate(rs.getTimestamp("rudate"));
//							rbbsDTO.setRcontent(rs.getString("rcontent"));
//							rbbsDTO.setRgood(rs.getInt("rgood"));
//							rbbsDTO.setRbad(rs.getInt("rbad"));
//							rbbsDTO.setRgroup(rs.getInt("rgroup"));
//							rbbsDTO.setRstep(rs.getInt("rstep"));
//							rbbsDTO.setRindent(rs.getInt("rindent"));
//							alist.add(rbbsDTO);
//						}	
//						return alist;
//				});		
		return list;
	}

//	@Override
//	public List<RbbsDTO> list(String bnum, int startRec, int endRec) throws Exception {
//		List<RbbsDTO> list = new ArrayList<>();
//		StringBuffer sql = new StringBuffer();
//		StringBuffer sql2 = new StringBuffer();
//		
//		//1) 입력 파라미터의 전체 목록을 구한다.
//		sql.append("select t2.* ");
//		sql.append("from (select row_number() over (order by rgroup desc, rstep asc) as num,t1.* ");
//		sql.append("		    from replyboard t1 ");
//		sql.append("			 where bnum=? ) t2 ");
//		sql.append("where num between ? and ? ");
//			
//		list = (List<RbbsDTO>)jdbcTemplate.query(
//				sql.toString(), 
//				new Object[] {bnum, startRec, endRec},
//				new BeanPropertyRowMapper(RbbsDTO.class)
//				);
//
//		//2) 부모참조댓글 정보만 추출 (rnum,rrnum)
//		sql2.append("select rnum,rrnum from replyboard where bnum= ? ");
//		
//		return list;
//	}
	
	@Override
	public List<RbbsDTO> list(String bnum, int startRec, int endRec) throws Exception {
		List<RbbsDTO> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer();

		sql.append("select x.*, ");
		sql.append("y.rnum rnum_1, y.bnum bnum_1, y.rid rid_1, y.rnickname rnickname_1, ");
		sql.append("y.rcdate rcdate_1, y.rudate rudate_1, y.rcontent rcontent_1, ");
		sql.append("y.rgood rgood_1, y.rbad rbad_1, y.rgroup rgroup_1, y.rstep rstep_1, ");
		sql.append("y.rindent rindent_1, y.isdel isdel_1, y.rrnum rrnum_1 ");		
		sql.append("  from (select t2.* ");
		sql.append("          from (select row_number() over (order by rgroup desc, rstep asc) as num,t1.* ");
		sql.append("                  from replyboard t1 ");
		sql.append("                 where bnum=? ) t2 ");
		sql.append("         where num between ? and ?) x, ");
		sql.append("        (select * ");
		sql.append("           from replyboard ");
		sql.append("          where bnum=? and rnum in (select distinct t4.rrnum ");
		sql.append("                            from (select row_number() over ");
		sql.append("                                          (order by rgroup desc, rstep asc) as num,t3.* ");
		sql.append("                                    from replyboard t3 ");
		sql.append("                                   where bnum=? and rrnum > 0) t4 ");
		sql.append("                           where num between ? and ?) ) y ");
		sql.append(" where x.rrnum = y.rnum(+) ");
		sql.append(" order by x.num ");
		
		list = jdbcTemplate.query(sql.toString(), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, Integer.parseInt(bnum));
				ps.setInt(2, startRec);
				ps.setInt(3, endRec);
				ps.setInt(4, Integer.parseInt(bnum));
				ps.setInt(5, Integer.parseInt(bnum));
				ps.setInt(6, startRec);
				ps.setInt(7, endRec);
			}
		},new RowMapper<RbbsDTO>() {

			@Override
			public RbbsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				RbbsDTO rbbsDTO = new RbbsDTO(); 
				rbbsDTO.setRnum(rs.getInt("rnum"));
				rbbsDTO.setBnum(rs.getInt("bnum"));
				rbbsDTO.setRid(rs.getString("rid"));
				rbbsDTO.setRnickname(rs.getString("rnickname"));
				rbbsDTO.setRcdate(rs.getTimestamp("rcdate"));
				rbbsDTO.setRudate(rs.getTimestamp("rudate"));
				rbbsDTO.setRcontent(rs.getString("rcontent"));
				rbbsDTO.setRgood(rs.getInt("rgood"));
				rbbsDTO.setRbad(rs.getInt("rbad"));
				rbbsDTO.setRgroup(rs.getInt("rgroup"));
				rbbsDTO.setRstep(rs.getInt("rstep"));
				rbbsDTO.setRindent(rs.getInt("rindent"));
				rbbsDTO.setIsdel(rs.getString("isdel"));
				rbbsDTO.setRrnum(rs.getInt("rrnum"));
				//대댓글 번호가 존재하면
				if(rs.getInt("rrnum") != 0) {
					RbbsDTO rrdto = new RbbsDTO();					
					rrdto.setRnum(rs.getInt("rnum_1"));
					rrdto.setBnum(rs.getInt("bnum_1"));
					rrdto.setRid(rs.getString("rid_1"));
					rrdto.setRnickname(rs.getString("rnickname_1"));
					rrdto.setRcdate(rs.getTimestamp("rcdate_1"));
					rrdto.setRudate(rs.getTimestamp("rudate_1"));
					rrdto.setRcontent(rs.getString("rcontent_1"));
					rrdto.setRgood(rs.getInt("rgood_1"));
					rrdto.setRbad(rs.getInt("rbad_1"));
					rrdto.setRgroup(rs.getInt("rgroup_1"));
					rrdto.setRstep(rs.getInt("rstep_1"));
					rrdto.setRindent(rs.getInt("rindent_1"));
					rrdto.setIsdel(rs.getString("isdel_1"));
					rrdto.setRrnum(rs.getInt("rrnum_1"));
					rbbsDTO.setRrdto(rrdto);
				}
						
				return rbbsDTO;
			}
		});
		return list;
	}
	
	//댓글 수정
	@Override
	public int modify(RbbsDTO rbbsDTO) throws Exception {
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("update replyboard set rudate=sysdate, rcontent=? ");
		sql.append("where rnum=? ");
		
		cnt = jdbcTemplate.update(sql.toString(),	rbbsDTO.getRcontent(), rbbsDTO.getRnum()); 

		return cnt;
	}
	//댓글 삭제
	@Override
	public int delete(String rnum) throws Exception {
		int cnt = 0;
		StringBuffer sql = new StringBuffer();

		// 답글존재유무 판단
		if(isReply(rnum)) {
			//답글 존재
			sql.append("update replyboard set isdel = 'Y' where rnum = ?");
			cnt = jdbcTemplate.update(sql.toString(),rnum);
			
		}else {
			//답글 미존재
			sql.append("delete from replyboard where rnum=?");
			cnt = jdbcTemplate.update(sql.toString(),rnum); 
		}	
		return cnt;
	}
	
	//원글에 대한 답글이 존재하는지 판단.
	private boolean isReply(String rnum) {
		boolean isYN = false;
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select count(rnum) ");
		sql.append("from replyboard ");
		sql.append("where bnum = (select bnum from replyboard ");
		sql.append("               where rnum = ?) ");
		sql.append("  and rrnum = ? ");
		
		cnt = jdbcTemplate.queryForObject(//sql문,파라미터,리턴타입
				sql.toString(),	new Object[] {rnum, rnum}, Integer.class);
		if(cnt > 0) {
			isYN = true;
		}
		return isYN;
	}	
	//댓글 호감 비호감
	@Override
	public int goodOrBad(String rnum, String goodOrBad) throws Exception {
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();		
		switch(goodOrBad) {
		case "good":
			sql.append("update replyboard set rgood = rgood + 1 where rnum=?");
			break;
		case "bad":
			sql.append("update replyboard set rbad = rbad + 1 where rnum=?");			
			break;
		default:
			break;
		}
		
		cnt = jdbcTemplate.update(sql.toString(),rnum); 

		return cnt;
	}
	//대댓글 등록
	@Override
	public int reply(RbbsDTO rbbsDTO) throws Exception {
		int cnt1=0 , cnt2=0;

		//댓글대상 정보 읽어오기
		RbbsDTO originDTO = replyView(rbbsDTO.getRrnum());

		//이전 답글 step 업데이트(원글그룹에 대한 세로정렬 재정의)
		cnt1 = updateStep(originDTO.getRgroup(), originDTO.getRstep());
		
		StringBuffer sql = new StringBuffer();	
		sql.append("INSERT INTO replyboard (rnum,bnum,rid,rnickname,rcontent,rgroup,rstep,rindent,rrnum) ");
		sql.append("values(rboardnum_seq.nextval,?,?,?,?,?,?,?,?) ");
		
		cnt2 = jdbcTemplate.update(
					sql.toString(),
					originDTO.getBnum(),
					rbbsDTO.getRid(),
					rbbsDTO.getRnickname(),
					rbbsDTO.getRcontent(),
					originDTO.getRgroup(),					//원글번호 = 원글 그룹
					originDTO.getRstep()+1,         //원글 그룹의 세로정렬(답글단계)
					originDTO.getRindent()+1,				//원글 그룹의 가로정렬(들여쓰기)
					rbbsDTO.getRrnum()
				);
		return cnt2;
	}
	
	//동일그룹의 댓글중에 동일스템의 글이 있으면 +1갱신
	private int updateStep(int rgroup, int rstep) {
		int cnt = 0;
		StringBuffer sql = new StringBuffer();
		sql.append("update replyboard set rstep=rstep+1 where rgroup = ? and rstep > ?");
		
		cnt = jdbcTemplate.update(sql.toString(), rgroup, rstep);		
		return cnt;
	}
	
	// 댓글대상 읽어오기
	private RbbsDTO replyView(int rnum) {
		RbbsDTO rdto = null;
		int cnt = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("select bnum,rgroup, rstep, rindent from replyBoard where rnum = ?");
		
		rdto = jdbcTemplate.queryForObject(sql.toString(), new RowMapper<RbbsDTO>() {
			@Override
			public RbbsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				RbbsDTO rdto = new RbbsDTO();
				rdto.setBnum(rs.getInt("bnum"));
				rdto.setRgroup(rs.getInt("rgroup"));
				rdto.setRstep(rs.getInt("rstep"));
				rdto.setRindent(rs.getInt("rindent"));				
				return rdto;
			}
			
		}, rnum);
		return rdto;	
	}
	//대댓글 총계
	@Override
	public int replyTotalRec(String bnum) throws Exception {
		int totalRec = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT count(*) totalRec from replyboard where bnum=?");
		
		totalRec = jdbcTemplate.queryForObject(
				sql.toString(), new Object[] {bnum}, Integer.class);
		
		return totalRec;
	}

}
