package springboard.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

public class EditCommand implements BbsCommandImpl{
	
	@Override
	public void execute(Model model){ 

		Map<String, Object> paramMap = model.asMap();
		HttpServletRequest req = (HttpServletRequest)paramMap.get("req");
		
		String idx = req.getParameter("idx");
		
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		
		SpringBbsDTO dto = dao.view(idx);
		model.addAttribute("viewRow", dto);
		dao.close();
	}
}
