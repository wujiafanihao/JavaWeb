package com.azyasaxi.service;

import com.azyasaxi.dao.ClassInfoDao;
import com.azyasaxi.dao.CreditSummaryDao;
import com.azyasaxi.dao.StudentDao;
import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.model.CreditSummary;
import com.azyasaxi.model.Student;
import org.springframework.beans.factory.annotation.Autowired; // 用于依赖注入
import org.springframework.stereotype.Service; // 声明这是一个 Service 组件
import org.springframework.util.StringUtils;

import java.math.BigDecimal; // 用于学分
import java.util.Collections;
import java.util.List;
import java.util.ArrayList; // 用于创建学生列表

/**
 * ClassInfoService 类 (服务层)
 * 负责处理与班级相关的业务逻辑，包括获取班级的详细信息及其学生列表。
 */
@Service
public class ClassInfoService {

    private final ClassInfoDao classInfoDao;         // ClassInfoDao 实例
    private final StudentDao studentDao;             // StudentDao 实例，用于获取班级下的学生
    private final CreditSummaryDao creditSummaryDao; // CreditSummaryDao 实例，用于获取学生总学分

    /**
     * 构造函数，通过 Spring 依赖注入 ClassInfoDao, StudentDao, 和 CreditSummaryDao。
     *
     * @param classInfoDaoArg     由 Spring 容器提供的 ClassInfoDao 实例。
     * @param studentDaoArg       由 Spring 容器提供的 StudentDao 实例。
     * @param creditSummaryDaoArg 由 Spring 容器提供的 CreditSummaryDao 实例。
     */
    public ClassInfoService(ClassInfoDao classInfoDaoArg, StudentDao studentDaoArg, CreditSummaryDao creditSummaryDaoArg) {
        this.classInfoDao = classInfoDaoArg;
        this.studentDao = studentDaoArg;
        this.creditSummaryDao = creditSummaryDaoArg;
    }
    public List<ClassInfo> listAllClassInfos() { // 方法名从 listAllStudents 改为 listAllClassInfos
        // 调用 DAO 层获取所有班级数据
        // System.out.println("Fetching all class infos from ClassInfoService..."); // 调试打印可以修改或移除
        return classInfoDao.getAllClassInfo();
    }

    // --- 原有方法 (保持或略作调整) ---
    public List<ClassInfo> searchClassInfos(String searchTerm) {
        try {
            return classInfoDao.searchClassInfos(searchTerm);
        } catch (Exception e) {
            System.err.println("ClassInfoService: 搜索班级信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean addClassInfo(ClassInfo classInfo) {
        if (classInfo == null || !StringUtils.hasText(classInfo.getClassName())) {
            System.err.println("ClassInfoService: 添加班级失败，班级名称不能为空。");
            return false;
        }
        // 业务逻辑：例如检查班级名在同一专业下是否已存在
         List<ClassInfo> existingClasses = classInfoDao.searchClassInfos(classInfo.getClassName());
         for (ClassInfo existing : existingClasses) {
             if (existing.getClassName().equalsIgnoreCase(classInfo.getClassName()) &&
                 ( (existing.getMajorId() == null && classInfo.getMajorId() == null) ||
                   (existing.getMajorId() != null && existing.getMajorId().equals(classInfo.getMajorId())) )
             ) {
                 System.err.println("ClassInfoService: 添加班级失败，名称为 '" + classInfo.getClassName() + "' 的班级已在该专业下存在。");
                 return false;
             }
         }
        try {
            int rowsAffected = classInfoDao.addClassInfo(classInfo);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("ClassInfoService: 添加班级时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClassInfo(int classId) {
        // (与您之前提供的代码一致)
        if (classId <= 0) {
            System.err.println("ClassInfoService: 删除班级失败，无效的班级ID: " + classId);
            return false;
        }
        try {
            int rowsAffected = classInfoDao.deleteClassInfo(classId);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("ClassInfoService: 删除班级 (ID: " + classId + ") 时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 获取班级完整详细信息的方法 ---
    /**
     * 根据班级ID获取该班级的完整详细信息。
     * 包括班级基本信息、专业、学院，以及该班级下的所有学生列表（每个学生包含ID、姓名、性别和总学分）。
     *
     * @param classId 班级的ID。
     * @return ClassInfo 对象，其中填充了所有详细信息；如果班级不存在，则返回 null。
     */
    public ClassInfo findClassInfoWithDetailsById(int classId) {
        if (classId <= 0) {
            System.err.println("ClassInfoService: 获取班级详细信息失败，无效的班级ID: " + classId);
            return null;
        }

        // 1. 获取班级基本信息 (包含专业名和学院名)
        ClassInfo classInfo = classInfoDao.getClassInfoWithHierarchyById(classId);

        if (classInfo == null) {
            System.out.println("ClassInfoService: 未找到ID为 " + classId + " 的班级。");
            return null; // 如果班级本身都找不到，则直接返回null
        }

        // 2. 获取该班级下的所有学生列表 (只需要学生基本信息)

        List<Student> studentsInThisClass = new ArrayList<>();
        try {
            studentsInThisClass = studentDao.getStudentsByClassId(classId); // 调用新的DAO方法
        } catch (Exception e) {
            System.err.println("ClassInfoService: 获取班级 (ID: " + classId + ") 的学生列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 即使学生列表获取失败，也返回已获取的班级信息
        }

        // 3. 为每个学生获取并设置其总学分
        if (!studentsInThisClass.isEmpty()) {
            for (Student student : studentsInThisClass) {
                try {
                    CreditSummary summary = creditSummaryDao.getCreditSummaryByStudentId(student.getStudentId());
                    if (summary != null) {
                        student.setTotalEarnedCredits(summary.getTotalCredits());
                    } else {
                        student.setTotalEarnedCredits(BigDecimal.ZERO); // 没有记录则总学分为0
                    }
                } catch (Exception e) {
                    System.err.println("ClassInfoService: 获取学生 (ID: " + student.getStudentId() + ") 的总学分时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    student.setTotalEarnedCredits(null); // 出错时设为null或0
                }
            }
        }

        classInfo.setStudentsInClass(studentsInThisClass); // 将学生列表（已填充学分）设置到ClassInfo对象中

        return classInfo; // 返回填充了所有详细信息的ClassInfo对象
    }

}