package springboard.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

public class ReplyCommand implements BbsCommandImpl{

	@Override
	public void execute(Model model) {
		Map<String, Object> map = model.asMap();
		HttpServletRequest req = (HttpServletRequest)map.get("req");
		
		String idx = req.getParameter("idx");
		
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		//기존게시물의 내용을 가져온다.
		SpringBbsDTO dto = dao.view(idx);
		//제목 앞에 답변글이라는 표현으로 [RE]를 추가한다.
		dto.setTitle("[RE]"+dto.getTitle());
		//내용에도 원본글을 표현하기 위해 문자열을 추가한다.
		dto.setContents("\n\r\n\r---[원본글]---\n\r"+dto.getContents());
		//모델 객체에 dto저장
		model.addAttribute("replyRow", dto);
		//dao.close();
	}
}
