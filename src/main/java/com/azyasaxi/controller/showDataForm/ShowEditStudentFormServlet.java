package com.azyasaxi.controller.showDataForm;

import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.model.Student;
import com.azyasaxi.service.ClassInfoService;
import com.azyasaxi.service.StudentService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/editStudent")
public class ShowEditStudentFormServlet extends HttpServlet {
    private StudentService studentService;
    private ClassInfoService classInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
            this.classInfoService = context.getBean(ClassInfoService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ShowEditStudentFormServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = "student"; // 编辑学生后通常返回学生模块

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int studentId = Integer.parseInt(idStr);
                // 使用 findStudentByIdForEdit 获取基本信息用于表单预填充
                Student studentToEdit = studentService.findStudentByIdForEdit(studentId); 
                List<ClassInfo> classList = classInfoService.listAllClassInfos(); // 获取所有班级用于下拉选择

                if (studentToEdit != null) {
                    request.setAttribute("editData", studentToEdit); // 将学生数据传递给JSP
                    request.setAttribute("classList", classList);   // 将班级列表传递给JSP
                    request.setAttribute("viewMode", "editStudent"); // 指示JSP显示学生编辑表单
                } else {
                    request.getSession().setAttribute("errorMessage", "未找到ID为 " + studentId + " 的学生以进行编辑。");
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                    return;
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的学生ID格式。");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                return;
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "加载学生编辑表单时发生错误：" + e.getMessage());
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                return;
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供学生ID，无法编辑。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
            return;
        }

        request.setAttribute("activeModule", activeModule);
        // 转发到通用的 detail.jsp 页面，由它根据 viewMode 渲染编辑表单
        request.getRequestDispatcher("/detail.jsp").forward(request, response);
    }
}