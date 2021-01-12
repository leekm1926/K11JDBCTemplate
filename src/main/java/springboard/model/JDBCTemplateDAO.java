package springboard.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;

/*
 JdbcTemplate 관련 주요메소드
 
 - List query(String sql, RowMapper rowMapper)
 	: 여러개의 레코드를 반환하는 select계열의 쿼리문인 경우 사용
 - List query(String sql, Object[] args, RowMapper rowMapper)
 	: 인파라미터를 가진 여러개의 레코드를 반환하는 select계열의
 	쿼리문인 경우 사용
 - Object queryForObject(String sql, RowMapper rowMapper)
 	: 하나의 레코드를 반환하는 select계열의 쿼리문 실행시 사용된다.
 - Object queryForObject(String sql, Object[] args, RowMapper rowMapper)
 	: 인파라미터가 있고, 하나의 레코드를 반환하는 select계열의 쿼리문 실행시 사용된다.
 - int queryForInt(String sql)
	: 쿼리문의 실행결과가 숫자를 반환하는 select계열의 쿼리문에 사용된다.
 - int queryForInt(String sql, Object[] args)
	: 인파라미터가 있고, 쿼리문의 실행결과가 숫자를 반환하는 select계열의 쿼리문에 사용된다.
- int update(String sql)
	: 인파라미터가 없는 update/delete/insert 쿼리문을 처리할 때 사용
- int update(String sql, Object[] args)
	: 인파라미터가 있는 update/delete/insert 쿼리문을 처리할 때 사용
 */
public class JDBCTemplateDAO {

	// 멤버변수
	JdbcTemplate template;

	// 생성자
	/*
	 * 컨트롤러에서 @Autowired를 통해 자동주입 받았던 빈을 정적변수인 JdbcTemplateConst.template에 값을
	 * 할당하였으므로, DB연결정보를 DAO에서 바로 사용하기위해 값을 가져온다.
	 */
	public JDBCTemplateDAO() {
		this.template = JdbcTemplateConst.template;
		System.out.println("JDBCTemplateDAO() 생성자 호출");
	}

	public void close() {
		// JDBCTemplate에서는 사용하지 않음.
		/*
		 * Spring 설정파일에서 빈을 생성하므로 자원을 해제하면 다시 new를 통해 생성해야하므로 자원해제를 하지 않는다.
		 */
	}

	// 게시물 수 카운트
	public int getTotalCount(Map<String, Object> map) {
		String sql = "SELECT COUNT(*) FROM springboard ";

		if (map.get("Word") != null) {
			sql += " WHERE " + map.get("Column") + " " + " 	LIKE '%" + map.get("Word") + "%' ";
		}
		// 쿼리문에서 count(*)를 통해 반환되는 값을 정수 형태로 반환한다.
		return template.queryForObject(sql, Integer.class);
	}

	// 게시판 리스트 가져오기(페이지처리X)
	public ArrayList<SpringBbsDTO> list(Map<String, Object> map) {
		String sql = "" + "SELECT * FROM springboard ";
		if (map.get("Word") != null) {
			sql += " WHERE " + map.get("Column") + " " + " 	LIKE '%" + map.get("Word") + "%' ";

		}
		// sql += " ORDER BY idx DESC"; // 일반게시판의 정렬방법
		sql += " ORDER BY bgroup DESC, bstep ASC"; // 답변형일 때 정렬방법
		/*
		 * RowMapper 객체가 select를 통해 얻어온 ResultSet을 DTO객체에 저장하고 List컬렉션에 적재하여 반환하게 된다.
		 */
		return (ArrayList<SpringBbsDTO>) template.query(sql,
				new BeanPropertyRowMapper<SpringBbsDTO>(SpringBbsDTO.class));
	}

	// 글쓰기 처리1
	public void write(final SpringBbsDTO springBbsDTO) {
		/*
		 * 매개변수로 전달되는 값을 익명클래스 내에서 사용할때는 반드시 final로 선언하여 값의 변경이 불가능하게 해줘야한다. final로 선언하지
		 * 않을 경우 에러가 발생한다.
		 */
		template.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {

				/*
				 * 답변형 게시판에서 원본글인 경우 idx와 bgroup은 반드시 일치해야한다. 리스트에서 게시물 정렬시 bgroup을 통해 order
				 * by절을 구성하기 때문이다. 또한 nextval은 한문장에서 여러번 사용해도 같은 시퀀스를 반환한다.
				 */
				String sql = "INSERT INTO springboard (" + " idx, name, title, contents, hits "
						+ " ,bgroup, bstep, bindent, pass) " + " VALUES (" + " springboard_seq.NEXTVAL,?,?,?,0,"
						+ " springboard_seq.NEXTVAL,0,0,?)";

				PreparedStatement psmt = con.prepareStatement(sql);
				psmt.setString(1, springBbsDTO.getName());
				psmt.setString(2, springBbsDTO.getTitle());
				psmt.setString(3, springBbsDTO.getContents());
				psmt.setString(4, springBbsDTO.getPass());

				return psmt;
			}

		});
	}

	// 조회수 증가
	public void updateHit(final String idx) {
		String sql = "UPDATE springboard SET " + " hits=hits+1 " + " WHERE idx=? ";

		template.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, Integer.parseInt(idx));
			}
		});
	}

	// 게시물 상세보기
	public SpringBbsDTO view(String idx) {
		// 조회수 증가 위해 호출
		updateHit(idx);

		SpringBbsDTO dto = new SpringBbsDTO();
		String sql = "SELECT * FROM springboard " + " WHERE idx=" + idx;
		/*
		 * queryForObject()는 반환결과가 0개이거나 2개 이상인 경우 예외가 발생되므로 반드시 예외처리를 해주는 것이 좋다.
		 */
		try {
			// 반환값이 하나만 있는 select계열의 쿼리문 실행
			dto = template.queryForObject(sql, new BeanPropertyRowMapper<SpringBbsDTO>(SpringBbsDTO.class));
		} catch (Exception e) {
			System.out.println("View()실행시 예외발생");
		}
		return dto;
	}

	public int password(String idx, String pass) {
		int retNum = 0;
		String sql = "SELECT * FROM springboard " + " WHERE pass=" + pass + " AND idx=" + idx;

		try {
			SpringBbsDTO dto = template.queryForObject(sql,
					new BeanPropertyRowMapper<SpringBbsDTO>(SpringBbsDTO.class));
			retNum = dto.getIdx();
		} catch (Exception e) {
			System.out.println("password() 예외발생");
		}
		return retNum;
	}

	public void edit(final SpringBbsDTO dto) {
		String sql = "UPDATE springboard " + " SET name=?, title=?, contents=?" + " WHERE idx=? AND pass=?";

		template.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, dto.getName());
				ps.setString(2, dto.getTitle());
				ps.setString(3, dto.getContents());
				ps.setInt(4, dto.getIdx());
				ps.setString(5, dto.getPass());
			}
		});
	}

	public void delete(final String idx, final String pass) {
		String sql = "DELETE FROM springboard WHERE idx=? AND pass=?";

		template.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, idx);
				ps.setString(2, pass);
			}
		});
	}

	// 답변글 입력
	public void reply(final SpringBbsDTO dto) {
		// 답변글을 입력하기전 일괄 업데이트 처리
		replyPrevUpdate(dto.getBgroup(), dto.getBstep());

		// write와 다른 점은 bgroup에 기존게시물의 번호가 들어가는 것이다.
		String sql = "INSERT INTO springboard " + " (idx, name, title, contents, pass, " + " bgroup, bstep, bindent) "
				+ " VALUES " + " (springboard_seq.nextval, ?, ?, ?, ?, " + " ?, ?, ?)";

		template.update(sql, new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, dto.getName());
				ps.setString(2, dto.getTitle());
				ps.setString(3, dto.getContents());
				ps.setString(4, dto.getPass());
				// 원본글의 group번호를 입력
				ps.setInt(5, dto.getBgroup());
				// 원본글의 step, indent에 +1한 후 입력
				ps.setInt(6, dto.getBstep() + 1);
				ps.setInt(7, dto.getBindent() + 1);
			}
		});
	}

	/*
	 답변글을 입력하기전 현재 step보다 큰 게시물들을 일괄적으로
	 step+1해서 뒤로 밀어주는 작업을 진행한다.
	 */
	public void replyPrevUpdate(final int strGroup, final int strStep) {
		String sql = "UPDATE springboard " + " SET bstep = bstep+1 " + " WHERE bgroup=? AND bstep>?";
		template.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, strGroup);
				ps.setInt(2, strStep);
			}

		});
	}

	// 리스트처리(페이지O)
	public ArrayList<SpringBbsDTO> listPage(Map<String, Object> map) {

		int start = Integer.parseInt(map.get("start").toString());
		int end = Integer.parseInt(map.get("end").toString());

		String sql = "" + "SELECT * FROM (" + "    SELECT Tb.*, rownum rNum FROM ("
				+ "        SELECT * FROM springboard ";
		if (map.get("Word") != null) {
			sql += " WHERE " + map.get("Column") + " " + " LIKE '%" + map.get("Word") + "%' ";
		}
		sql += " ORDER BY bgroup DESC, bstep ASC" + "    ) Tb" + ")" + " WHERE rNum BETWEEN " + start + " and " + end;

		return (ArrayList<SpringBbsDTO>) template.query(sql,
				new BeanPropertyRowMapper<SpringBbsDTO>(SpringBbsDTO.class));
	}

}
