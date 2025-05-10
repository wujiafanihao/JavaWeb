package com.azyasaxi.service;

import com.azyasaxi.dao.MajorDao;
import com.azyasaxi.model.Major;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MajorService 类 (服务层)
 * 负责处理与专业相关的业务逻辑。
 */
@Service
public class MajorService {

    private final MajorDao majorDao;

    @Autowired
    public MajorService(MajorDao majorDao) {
        this.majorDao = majorDao;
    }

    /**
     * 获取所有专业的列表。
     *
     * @return 包含所有 Major 对象的列表。
     */
    public List<Major> listAllMajors() {
        return majorDao.getAllMajors();
    }

    // 未来可以添加其他与专业相关的业务逻辑方法
}