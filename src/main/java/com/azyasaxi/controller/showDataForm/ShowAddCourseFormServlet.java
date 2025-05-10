package com.azyasaxi.controller.showDataForm;

import com.azyasaxi.model.Major; // 导入 Major 模型类
import com.azyasaxi.service.MajorService; // 导入 MajorService 类
import jakarta.servlet.ServletConfig; // 导入 ServletConfig
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext; // 导入 WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils; // 导入 WebApplicationContextUtils

import java.io.IOException;
import java.util.Collections; // 用于返回空列表
import java.util.List; // 导入 List

/**
 * ShowAddCourseFormServlet (控制器)
 * 负责显示用于添加新课程的表单页面 (addCourseForm.jsp)。
 * 它会预先加载所有专业列表，以便用户在表单中为课程选择所属专业。
 */
@WebServlet("/admin/showAddCourseForm") // Servlet的映射路径
public class ShowAddCourseFormServlet extends HttpServlet {

    private MajorService majorService; // MajorService 实例，用于获取专业数据

    /**
     * Servlet 初始化方法。
     * 在 Servlet 第一次被加载时调用。
     * 此方法从 Spring 的 WebApplicationContext 中获取 MajorService bean。
     * @param config Servlet 配置对象，用于获取 ServletContext。
     * @throws ServletException 如果初始化过程中发生错误，例如 Spring 上下文未找到或bean无法获取。
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // 调用父类的 init 方法
        // 从 ServletContext 获取 Spring WebApplicationContext
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            // 从 Spring 上下文中获取 MajorService bean
            this.majorService = context.getBean(MajorService.class);
        } else {
            // 如果 Spring 上下文未初始化 (例如，web.xml 中缺少 ContextLoaderListener)，则抛出异常
            throw new ServletException("Spring WebApplicationContext 在 ShowAddCourseFormServlet 中未找到。请确保 ContextLoaderListener 已在 web.xml 中配置。");
        }
    }

    /**
     * 处理 HTTP GET 请求。
     * 此方法会从 MajorService 获取所有专业列表，
     * 将专业列表设置到请求属性 (request attribute) 中，
     * 然后将请求转发到 addCourseForm.jsp 页面进行渲染。
     *
     * @param request HttpServletRequest 对象，代表客户端的HTTP请求。
     * @param response HttpServletResponse 对象，代表服务器将发送给客户端的HTTP响应。
     * @throws ServletException 如果在处理请求时发生Servlet特定的错误。
     * @throws IOException 如果发生输入或输出相关的错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 检查 MajorService 是否已成功初始化
        if (this.majorService == null) {
            System.err.println("ShowAddCourseFormServlet: MajorService 未初始化。"); // 记录错误到服务器日志
            // 向客户端发送一个内部服务器错误响应
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "依赖服务 MajorService 未正确初始化，无法加载课程添加表单。");
            return;
        }

        List<Major> majorList = Collections.emptyList(); // 初始化为空列表，以防出错时传递null
        try {
            // 调用 MajorService 获取所有专业信息
            majorList = majorService.listAllMajors();
            // 将获取到的专业列表设置到请求属性中，以便JSP页面可以访问
            request.setAttribute("majorListFromServlet", majorList); // 使用不同的变量名
        } catch (Exception e) {
            // 如果在获取专业列表过程中发生异常
            System.err.println("ShowAddCourseFormServlet: 加载专业列表以供添加课程表单使用时出错: " + e.getMessage());
            e.printStackTrace(); // 打印完整的异常堆栈到服务器日志
            // 设置一个错误消息到请求属性中，JSP页面可以显示这个错误
            request.setAttribute("errorMessageForMajorList", "无法加载专业列表，请稍后重试或联系管理员。");
            // 即使加载专业失败，仍然尝试显示表单，但专业下拉列表将是空的或显示错误提示
        }

        // 将请求转发到 addCourseForm.jsp 页面
        request.getRequestDispatcher("/addCourseForm.jsp").forward(request, response);
    }
}