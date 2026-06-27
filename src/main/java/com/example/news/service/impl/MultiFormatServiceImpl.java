package com.example.news.service.impl;

import com.example.news.service.DeepSeekService;
import com.example.news.service.MultiFormatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiFormatServiceImpl implements MultiFormatService {

    private final DeepSeekService deepSeekService;

    @Value("${tesseract.datapath:.}")
    private String tessDataPath;

    @Value("${tesseract.language:chi_sim+eng}")
    private String tessLanguage;

    @Override
    public String processAndSummarize(String text, MultipartFile file) {
        String extractedText;

        // 1. 如果有上传文件，优先解析文件
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("文件名无效");
            }
            String lowerName = originalFilename.toLowerCase();

            try {
                if (lowerName.endsWith(".docx")) {
                    extractedText = extractDocxText(file);
                } else if (isImageFile(lowerName)) {
                    extractedText = extractImageText(file);
                } else {
                    throw new RuntimeException("不支持的文件格式: " + originalFilename);
                }
            } catch (IOException e) {
                log.error("文件解析失败", e);
                throw new RuntimeException("文件解析失败: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("请上传文件");
        }

        // 2. 调用 DeepSeek 总结
        return deepSeekService.summarize(extractedText);
    }

    private boolean isImageFile(String filename) {
        return filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                || filename.endsWith(".png") || filename.endsWith(".gif")
                || filename.endsWith(".bmp") || filename.endsWith(".webp");
    }


    private String extractDocxText(MultipartFile file) throws IOException {
        try (InputStream is = new ByteArrayInputStream(file.getBytes());
             XWPFDocument docx = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        }
    }

    private String extractImageText(MultipartFile file) throws IOException {
        ITesseract tesseract = new Tesseract();
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            tesseract.setDatapath(tessDataPath);
        }
        tesseract.setLanguage(tessLanguage);

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                throw new RuntimeException("无法读取图片，请确保图片格式正确");
            }
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            log.error("OCR 识别失败", e);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage());
        }
    }
}
