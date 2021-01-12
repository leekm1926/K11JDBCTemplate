package springboard.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

public class WriteActionCommand implements BbsCommandImpl{

    @Override
    public void execute(Model model) {
    	
    	//파라미터 한번에 전달받기
        Map<String,Object> paramMap = model.asMap();
        //request객체와 DTO객체를 형변환 후 가져옴.
        HttpServletRequest req = (HttpServletRequest)paramMap.get("req");
        SpringBbsDTO springBbsDTO = (SpringBbsDTO)paramMap.get("springBbsDTO");

        System.out.println("springBbsDTO.title="+springBbsDTO.getTitle());

        JDBCTemplateDAO dao = new JDBCTemplateDAO();

        dao.write(springBbsDTO);
//        dao.close();
    }
}
