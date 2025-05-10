package com.azyasaxi.service;

import com.azyasaxi.dao.ClassInfoDao;
import com.azyasaxi.model.ClassInfo;
import org.springframework.beans.factory.annotation.Autowired; // 用于依赖注入
import org.springframework.stereotype.Service; // 声明这是一个 Service 组件

import java.util.List;

/**
 * ClassInfoService 类 (服务层)
 * 负责处理与学生相关的业务逻辑。
 * 它会调用 ClassInfoDao 来与数据库进行交互。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class ClassInfoService {

    private final ClassInfoDao classInfoDao; // ClassInfoDao 实例，通过构造函数注入

    /**
     * 构造函数，通过 Spring 依赖注入 ClassInfoDao。
     *
     * @param classInfoDao 由 Spring 容器提供的 ClassInfoDao 实例。
     */
    @Autowired // 自动注入 StudentDao bean
    public ClassInfoService(ClassInfoDao classInfoDao) {
        this.classInfoDao = classInfoDao;
    }

    /**
     * 获取所有班级信息的列表。
     *
     * @return 包含所有 ClassInfo 对象的列表。
     */
    public List<ClassInfo> listAllClassInfos() { // 方法名从 listAllStudents 改为 listAllClassInfos
        // 调用 DAO 层获取所有班级数据
        // System.out.println("Fetching all class infos from ClassInfoService..."); // 调试打印可以修改或移除
        return classInfoDao.getAllClassInfo();
    }

    /**
     * 根据搜索词搜索班级信息。
     *
     * @param searchTerm 用于在班级名称、专业名称或学院名称中搜索的词。
     * @return 包含匹配 ClassInfo 对象的列表。
     */
    public List<ClassInfo> searchClassInfos(String searchTerm) {
        // 调用 DAO 层执行搜索
        return classInfoDao.searchClassInfos(searchTerm);
    }

    /**
     * 添加一个新的班级信息。
     *
     * @param classInfo 要添加的班级信息对象。
     * @return 如果添加成功，返回 true；否则返回 false。
     *         （DAO 层返回影响的行数，这里可以转换为 boolean）
     */
    public boolean addClassInfo(ClassInfo classInfo) {
        if (classInfo == null || classInfo.getClassName() == null || classInfo.getClassName().trim().isEmpty()) {
            // 基本的验证，例如班级名称不能为空
            System.err.println("ClassInfoService: 添加班级失败，班级名称不能为空。");
            return false;
        }
        // 调用 DAO 层添加班级
        int rowsAffected = classInfoDao.addClassInfo(classInfo);
        return rowsAffected > 0; // 如果影响行数大于0，则表示添加成功
    }

    // 未来可以根据需求添加其他班级相关的业务逻辑方法，例如：
    // public ClassInfo findClassInfoById(int classId) { ... }
    // public boolean updateClassInfo(ClassInfo classInfo) { ... }
    // public boolean deleteClassInfo(int classId) { ... }
}