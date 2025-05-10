package com.azyasaxi.utils;

import java.nio.charset.StandardCharsets; // 导入标准字符集类，用于指定字符编码
import java.security.MessageDigest; // 导入MessageDigest类，用于生成信息摘要
import java.security.NoSuchAlgorithmException; // 导入NoSuchAlgorithmException类，处理不存在的算法异常

public class CalculateSHA256 {

    /**
     * 计算给定文本的 SHA-256 哈希值
     *
     * @param text 要计算哈希值的文本
     * @return 文本的 SHA-256 哈希值的 Hex 字符串表示
     *
     * 此方法使用 SHA-256 加密算法来计算输入文本的哈希值它首先获取 SHA-256 加密器的实例，
     * 然后将输入文本转换为 UTF-8 字节序列并计算哈希值最后，它将哈希值转换为 Hex 字符串并返回
     *
     * 注意：如果 Java 环境不支持 SHA-256 算法，此方法将抛出 RuntimeException 异常
     */
    public static String calculateSHA256(String text) {
        try {
            // 获取 SHA-256 加密器实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 计算输入文本的哈希值
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            // 转换为 Hex 字符串
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // 如果 Java 环境不支持 SHA-256 算法，抛出运行时异常
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

}
