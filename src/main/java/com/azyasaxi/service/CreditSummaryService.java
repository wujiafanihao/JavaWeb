package com.azyasaxi.service;

import com.azyasaxi.dao.CreditSummaryDao; // 导入 CreditSummaryDao
import com.azyasaxi.model.CreditSummary;   // 导入 CreditSummary 模型
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections; // 用于返回空列表
import java.util.List;

/**
 * CreditSummaryService 类 (服务层)
 * 负责处理与学生学分统计相关的业务逻辑。
 * 它会调用 CreditSummaryDao 来从 CreditSummary 视图获取数据。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class CreditSummaryService {

    private final CreditSummaryDao creditSummaryDao; // CreditSummaryDao 实例，通过构造函数注入

    /**
     * 构造函数，通过 Spring 依赖注入 CreditSummaryDao。
     *
     * @param creditSummaryDaoArg 由 Spring 容器提供的 CreditSummaryDao 实例。
     */
    @Autowired // 自动注入 CreditSummaryDao bean
    public CreditSummaryService(CreditSummaryDao creditSummaryDaoArg) {
        this.creditSummaryDao = creditSummaryDaoArg;
    }

    /**
     * 获取所有学生的学分统计信息列表，或者根据学生姓名或学号进行搜索。
     *
     * @param searchTerm 用于在学生姓名或学号中搜索的词 (可选)。
     * @return 包含匹配 CreditSummary 对象的列表。
     */
    public List<CreditSummary> listCreditSummaries(String searchTerm) {
        // 直接调用 DAO 层的搜索方法
        try {
            if (searchTerm != null && searchTerm.trim().isEmpty()) {
                // 如果搜索词是空字符串，则视为搜索所有
                searchTerm = null;
            }
            return creditSummaryDao.searchCreditSummaries(searchTerm);
        } catch (Exception e) {
            // 如果在获取数据过程中发生异常，打印错误日志并返回空列表
            System.err.println("CreditSummaryService: 获取学分统计列表时发生错误: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息
            return Collections.emptyList(); // 返回空列表以避免null指针
        }
    }

    /**
     * 根据学生ID获取该学生的学分统计信息。
     *
     * @param studentId 学生的ID。
     * @return 如果找到，返回 CreditSummary 对象；否则返回 null。
     */
    public CreditSummary findCreditSummaryByStudentId(int studentId) {
        // 对学生ID进行基本验证
        if (studentId <= 0) {
            System.err.println("CreditSummaryService: 无效的学生ID: " + studentId);
            return null; // 无效ID直接返回null
        }
        try {
            return creditSummaryDao.getCreditSummaryByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("CreditSummaryService: 根据学生ID查找学分统计时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null; // 出错时返回null
        }
    }

    // 针对学分统计，通常业务逻辑会集中在如何展示这些数据，
    // 或者基于这些数据进行一些分析，例如：
    // - 获取学分最高的N个学生
    // - 计算平均学分等
    // 由于数据源是视图，修改操作不在此处进行。
    // 例如，如果需要导出学分报告，可以在这里添加相应的方法。
}