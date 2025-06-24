package kintai;


import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/ProjectManageServlet")
public class ProjectManageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private ProjectManageDao ProjectManageDao = new ProjectManageDao();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse 
			response) throws ServletException, IOException {
		
		List<ProjectManageBean> projectmanagelist = ProjectManageDao.findAll();
		request.setAttribute("projectmanagelist", projectmanagelist);
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/web/projectmanage.jsp");
        dispatcher.forward(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse 
			response) throws ServletException, IOException {
		doGet(request, response);
	}

}
