package com.example.news.service;

import org.springframework.web.multipart.MultipartFile;

public interface MultiFormatService {

    /**
     * 处理多格式文本（纯文本、doc/docx、图片OCR）并调用DeepSeek总结
     * @param text 纯文本内容（可选）
     * @param file 上传的文件（doc/docx/图片）
     * @return 总结结果
     */
    String processAndSummarize(String text, MultipartFile file);
}
