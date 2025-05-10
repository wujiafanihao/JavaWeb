package com.azyasaxi.service;

import com.azyasaxi.dao.StudentDao; // 导入 StudentDao 类
import com.azyasaxi.model.Student;   // 导入 Student 模型类
import org.springframework.beans.factory.annotation.Autowired; // 用于依赖注入
import org.springframework.stereotype.Service; // 声明这是一个 Service 组件

import java.util.List;

/**
 * StudentService 类 (服务层)
 * 负责处理与学生相关的业务逻辑。
 * 它会调用 StudentDao 来与数据库进行交互。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class StudentService {

    private final StudentDao studentDao; // StudentDao 实例，通过构造函数注入

    /**
     * 构造函数，通过 Spring 依赖注入 StudentDao。
     *
     * @param studentDao 由 Spring 容器提供的 StudentDao 实例。
     */
    @Autowired // 自动注入 StudentDao bean
    public StudentService(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    /**
     * 获取所有学生的列表。
     *
     * @return 包含所有 Student 对象的列表。
     */
    public List<Student> listAllStudents() {
        // 调用 DAO 层获取所有学生数据
        System.out.println("student:"+studentDao.getAllStudents());
        return studentDao.getAllStudents();
    }

    /**
     * 根据搜索词搜索学生信息。
     * 搜索基于学生姓名或班级名称。
     *
     * @param searchTerm 用于在学生姓名或班级名称中搜索的词。
     * @return 包含匹配 Student 对象的列表。
     */
    public List<Student> searchStudents(String searchTerm) {
        // 调用 DAO 层执行搜索
        // System.out.println("StudentService: Searching students with term: " + searchTerm); // 调试打印
        return studentDao.searchStudents(searchTerm);
    }

    /**
     * 添加一个新的学生信息。
     *
     * @param student 要添加的学生信息对象。
     * @return 如果添加成功，返回 true；否则返回 false。
     */
    public boolean addStudent(Student student) {
        if (student == null || student.getName() == null || student.getName().trim().isEmpty() ||
            student.getUserName() == null || student.getUserName().trim().isEmpty() ||
            student.getPassword() == null || student.getPassword().isEmpty()) {
            // 基本的验证
            System.err.println("StudentService: 添加学生失败，姓名、用户名和密码不能为空。");
            return false;
        }
        // TODO: 在实际应用中，这里应该对密码进行加密处理
        // student.setPassword(passwordEncoder.encode(student.getPassword()));

        // TODO: 可以添加更复杂的业务逻辑，例如检查用户名是否已存在（虽然DAO层和数据库层面也会处理）
        // if (studentDao.findStudentByUsername(student.getUserName()) != null) {
        //     System.err.println("StudentService: 用户名 " + student.getUserName() + " 已存在。");
        //     return false;
        // }

        int rowsAffected = studentDao.addStudent(student);
        return rowsAffected > 0;
    }

    // 未来可以根据需求添加其他学生相关的业务逻辑方法，例如：
    // public Student findStudentById(int studentId) { ... }
    // public boolean updateStudentDetails(Student student) { ... }
    // public boolean deleteStudent(int studentId) { ... }
}