package com.azyasaxi.service;

import com.azyasaxi.dao.CourseDao; // 导入 CourseDao
import com.azyasaxi.model.Course;   // 导入 Course 模型
import com.azyasaxi.model.Enrollment; // 导入 Enrollment 模型
import com.azyasaxi.service.EnrollmentService; // 导入 EnrollmentService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils; // 用于检查集合是否为空

import java.math.BigDecimal; // 用于处理学分
import java.util.Collections;
import java.util.List;

/**
 * CourseService 类 (服务层)
 * 负责处理与课程相关的业务逻辑。
 * 它会调用 CourseDao 来与数据库进行交互，并可能调用 EnrollmentService 获取选课信息。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class CourseService {

    private final CourseDao courseDao; // CourseDao 实例
    private final EnrollmentService enrollmentService; // EnrollmentService 实例

    /**
     * 构造函数，通过 Spring 依赖注入 CourseDao 和 EnrollmentService。
     *
     * @param courseDao 由 Spring 容器提供的 CourseDao 实例。
     * @param enrollmentService 由 Spring 容器提供的 EnrollmentService 实例。
     */
    @Autowired // 自动注入 beans
    public CourseService(CourseDao courseDao, EnrollmentService enrollmentService) {
        this.courseDao = courseDao;
        this.enrollmentService = enrollmentService;
    }

    /**
     * 获取所有课程信息的列表。
     *
     * @return 包含所有 Course 对象的列表。
     */
    public List<Course> listAllCourses() {
        // 调用 DAO 层获取所有课程数据
        return courseDao.getAllCourses();
    }

    /**
     * 根据搜索词搜索课程信息。
     *
     * @param searchTerm 用于在课程名称或课程教师中搜索的词。
     * @return 包含匹配 Course 对象的列表。
     */
    public List<Course> searchCourses(String searchTerm) {
        // 调用 DAO 层执行搜索
        return courseDao.searchCourses(searchTerm);
    }

    /**
     * 添加一个新的课程信息。
     *
     * @param courseName  课程的名称。
     * @param teacherName 课程的教师名称。
     * @param credit      课程的学分。
     * @param majorName   课程所属的专业名称 (可以为 null 或空，表示不关联专业或全校选修)。
     * @return 如果添加成功，返回 true；否则返回 false。
     *         （DAO 层返回影响的行数或特定错误码，这里可以转换为 boolean）
     */
    public boolean addCourse(String courseName, String teacherName, BigDecimal credit, String majorName) {
        // 基本的验证
        if (courseName == null || courseName.trim().isEmpty()) {
            System.err.println("CourseService: 添加课程失败，课程名称不能为空。");
            return false;
        }
        if (teacherName == null || teacherName.trim().isEmpty()) {
            System.err.println("CourseService: 添加课程失败，教师名称不能为空。");
            return false;
        }
        if (credit == null || credit.compareTo(BigDecimal.ZERO) < 0) { // 学分不能为null且不能为负
            System.err.println("CourseService: 添加课程失败，学分无效。");
            return false;
        }

        Course newCourse = new Course();
        newCourse.setCourseName(courseName.trim());
        newCourse.setCourseTeacher(teacherName.trim());
        newCourse.setCredit(credit);
        // majorName 可以为 null 或空字符串，DAO层会处理

        // 调用 DAO 层添加课程
        // CourseDao的addCourse方法返回影响的行数，或者特定的负数错误码
        int result = courseDao.addCourse(newCourse, (majorName != null ? majorName.trim() : null));

        if (result > 0) {
            return true; // 添加成功
        } else {
            // 可以根据DAO层返回的负数错误码给出更详细的日志或错误信息
            if (result == -1) {
                System.err.println("CourseService: 添加课程失败，因为找不到指定的专业名称: " + majorName);
            } else if (result == -2) {
                System.err.println("CourseService: 添加课程失败，查询专业ID时发生数据库错误。");
            } else if (result == -3) {
                System.err.println("CourseService: 添加课程失败，课程 '" + courseName + "' 在该专业下已存在。");
            } else {
                System.err.println("CourseService: 添加课程失败，DAO层返回未知错误或影响行数为0。");
            }
            return false; // 添加失败
        }
    }

    /**
     * 根据课程ID获取课程的详细信息，包括选修该课程的学生列表及其成绩。
     *
     * @param courseId 课程的ID。
     * @return Course 对象，包含基本信息和选课学生列表；如果课程不存在，则返回 null。
     */
    public Course getCourseDetailsById(int courseId) {
        if (courseId <= 0) {
            System.err.println("CourseService: 无效的课程ID: " + courseId);
            return null;
        }
        Course course = courseDao.getCourseById(courseId); // 获取课程基本信息
        if (course != null) {
            // 获取选修该课程的学生列表 (包含学生详情和成绩)
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseIdWithStudentDetails(courseId);
            course.setEnrollments(enrollments); // 设置到 Course 对象的 enrollments 字段
        } else {
            System.out.println("CourseService: 未找到ID为 " + courseId + " 的课程。");
        }
        return course;
    }

    // findCourseById 可以保留，或者让调用者都使用 getCourseDetailsById
    // 为了清晰，如果只需要基本信息，可以保留 findCourseById
     public Course findCourseById(int courseId) {
        if (courseId <= 0) {
             System.err.println("CourseService: 无效的课程ID (findCourseById): " + courseId);
             return null;
        }
        return courseDao.getCourseById(courseId);
    }


    /**
     * 更新现有的课程信息。
     *
     * @param courseId    要更新的课程的ID。
     * @param courseName  新的课程名称。
     * @param teacherName 新的教师名称。
     * @param credit      新的学分。
     * @param majorName   新的专业名称 (可以为 null 或空，表示不更新或清除专业关联)。
     * @return 如果更新成功，返回 true；否则返回 false。
     */
    public boolean updateCourse(int courseId, String courseName, String teacherName, BigDecimal credit, String majorName) {
        // 验证输入
        if (courseName == null || courseName.trim().isEmpty()) {
            System.err.println("CourseService: 更新课程失败，课程名称不能为空。");
            return false;
        }
        // ... 其他验证可以添加 ...

        Course courseToUpdate = courseDao.getCourseById(courseId);
        if (courseToUpdate == null) {
            System.err.println("CourseService: 更新课程失败，未找到ID为 " + courseId + " 的课程。");
            return false;
        }

        courseToUpdate.setCourseName(courseName.trim());
        courseToUpdate.setCourseTeacher(teacherName.trim());
        courseToUpdate.setCredit(credit);
        // majorName 的处理逻辑在DAO层

        int result = courseDao.updateCourse(courseToUpdate, (majorName != null ? majorName.trim() : null));
        if (result > 0) {
            return true;
        } else {
            // 可以根据DAO层返回的错误码进行更详细的日志
            System.err.println("CourseService: 更新课程 (ID: " + courseId + ") 失败。");
            return false;
        }
    }

    /**
     * 根据课程ID删除课程。
     *
     * @param courseId 要删除的课程的ID。
     * @return 如果删除成功，返回 true；否则返回 false。
     */
    public boolean deleteCourse(int courseId) {
        // 可以在这里添加业务逻辑，例如检查是否有学生正在修读此课程且不允许删除等
        // 但根据您的表结构，Enrollment表有级联删除，所以直接删除即可

        int rowsAffected = courseDao.deleteCourse(courseId);
        if (rowsAffected > 0) {
            return true;
        } else {
            // 可能是因为课程不存在，或者删除时发生数据库错误
            System.err.println("CourseService: 删除课程 (ID: " + courseId + ") 失败或课程不存在。");
            return false;
        }
    }
}