package com.azyasaxi.controller.admin;

import com.azyasaxi.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper; // 用于将Java对象转换为JSON字符串
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/admin/dataStats")
public class DataStatsServlet extends HttpServlet {
    private StudentService studentService;
    private ObjectMapper objectMapper; // Jackson ObjectMapper for JSON conversion

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
            this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper
        } else {
            throw new ServletException("Spring WebApplicationContext not found for DataStatsServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (this.studentService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "StudentService 未正确初始化。");
            return;
        }

        try {
            // 1. 获取按学院统计的数据
            List<Map<String, Object>> collegeStatsRaw = studentService.getStudentStatsByCollege();
            // 转换为 Chart.js 需要的格式
            List<String> collegeNames = new ArrayList<>();
            List<Number> studentCountsPerCollege = new ArrayList<>();
            List<Number> avgCreditsPerCollege = new ArrayList<>();

            for (Map<String, Object> stat : collegeStatsRaw) {
                collegeNames.add((String) stat.getOrDefault("college_name", "未知学院"));
                studentCountsPerCollege.add((Number) stat.getOrDefault("student_count", 0));
                // AVG 可能返回 BigDecimal 或 Double，确保转换为 Number
                Object avgCreditsObj = stat.get("average_total_credits");
                avgCreditsPerCollege.add(avgCreditsObj instanceof Number ? (Number) avgCreditsObj : 0.0);
            }
            request.setAttribute("collegeNamesJson", objectMapper.writeValueAsString(collegeNames));
            request.setAttribute("studentCountsPerCollegeJson", objectMapper.writeValueAsString(studentCountsPerCollege));
            request.setAttribute("avgCreditsPerCollegeJson", objectMapper.writeValueAsString(avgCreditsPerCollege));

            // 2. 获取按学分区间统计的数据
            List<Map<String, Object>> creditRangeStatsRaw = studentService.getStudentCountByCreditRanges();
            // 转换为 Chart.js 需要的格式
            List<String> creditRangeLabels = creditRangeStatsRaw.stream()
                                                .map(stat -> (String) stat.getOrDefault("credit_range", "未知区间"))
                                                .collect(Collectors.toList());
            List<Number> studentCountsPerRange = creditRangeStatsRaw.stream()
                                                .map(stat -> (Number) stat.getOrDefault("student_count", 0))
                                                .collect(Collectors.toList());

            request.setAttribute("creditRangeLabelsJson", objectMapper.writeValueAsString(creditRangeLabels));
            request.setAttribute("studentCountsPerRangeJson", objectMapper.writeValueAsString(studentCountsPerRange));

            // 设置一个标记，表示数据已加载，JSP可以尝试渲染图表
            request.setAttribute("statsDataLoaded", true);

        } catch (Exception e) {
            System.err.println("DataStatsServlet: 获取或处理统计数据时出错: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("statsErrorMessage", "加载统计数据时发生内部错误。");
            request.setAttribute("statsDataLoaded", false);
        }

        request.getRequestDispatcher("/dataStats.jsp").forward(request, response);
    }
}