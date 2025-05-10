package com.azyasaxi.service;

import com.azyasaxi.dao.EnrollmentDao;
import com.azyasaxi.model.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // 导入 BigDecimal
import java.util.Collections;
import java.util.List;

/**
 * EnrollmentService 类 (服务层)
 * 负责处理与选课记录相关的业务逻辑。
 */
@Service
public class EnrollmentService {

    private final EnrollmentDao enrollmentDao;

    @Autowired
    public EnrollmentService(EnrollmentDao enrollmentDao) {
        this.enrollmentDao = enrollmentDao;
    }

    /**
     * 根据课程ID获取所有选课记录，并包含选课学生的详细信息。
     *
     * @param courseId 课程的ID。
     * @return 包含 Enrollment 对象的列表。
     */
    public List<Enrollment> getEnrollmentsByCourseIdWithStudentDetails(int courseId) {
        if (courseId <= 0) {
            System.err.println("EnrollmentService: 无效的课程ID: " + courseId);
            return Collections.emptyList();
        }
        try {
            return enrollmentDao.getEnrollmentsByCourseIdWithStudentDetails(courseId);
        } catch (Exception e) {
            System.err.println("EnrollmentService: 根据课程ID获取选课学生列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 更新特定学生特定课程的成绩。
     *
     * @param studentId 学生的ID。
     * @param courseId  课程的ID。
     * @param newGrade  新的成绩。允许为 null (表示成绩未录入或清除)。
     *                  如果非null，应在0到100之间（或您设定的其他有效范围）。
     * @return 如果更新成功，返回 true；否则返回 false。
     */
    public boolean updateStudentCourseGrade(int studentId, int courseId, BigDecimal newGrade) {
        if (studentId <= 0 || courseId <= 0) {
            System.err.println("EnrollmentService: 更新成绩失败，无效的学生ID或课程ID。");
            return false;
        }

        // 业务逻辑验证：例如成绩范围
        if (newGrade != null) {
            if (newGrade.compareTo(BigDecimal.ZERO) < 0 || newGrade.compareTo(new BigDecimal("100")) > 0) {
                System.err.println("EnrollmentService: 更新成绩失败，成绩必须在0到100之间 (或为null)。当前值: " + newGrade);
                return false;
            }
        }

        try {
            int rowsAffected = enrollmentDao.updateGradeByStudentAndCourse(studentId, courseId, newGrade);
            return rowsAffected > 0; // 如果影响行数大于0，则表示更新成功
        } catch (Exception e) {
            System.err.println("EnrollmentService: 更新学生 (ID: " + studentId + ") 课程 (ID: " + courseId + ") 成绩时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 未来可以添加其他与选课相关的业务逻辑方法，例如：
    // public boolean enrollStudentInCourse(int studentId, int courseId) { ... }
    // public boolean deleteEnrollment(int enrollmentId) { ... }
}