package com.azyasaxi.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * ShowAddClassFormServlet
 * 负责显示用于添加新班级的表单页面。
 */
@WebServlet("/admin/showAddClassForm")
public class ShowAddClassFormServlet extends HttpServlet {

    /**
     * 处理 GET 请求。
     * 转发到添加班级的 JSP 页面 (addClassForm.jsp)。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 在实际应用中，这里可能需要加载专业列表 (List<Major>) 并将其设置到请求属性中，
        // 以便在 addClassForm.jsp 中以下拉列表的形式供用户选择班级所属的专业。
        // request.setAttribute("majorList", majorService.getAllMajors());

        // 直接转发到表单页面
        request.getRequestDispatcher("/addClassForm.jsp").forward(request, response);
    }
}