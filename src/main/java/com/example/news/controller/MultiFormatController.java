package com.example.news.controller;

import com.example.news.service.MultiFormatService;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/multi-format")
@RequiredArgsConstructor
public class MultiFormatController {

    private final MultiFormatService multiFormatService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> process(
            @RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请上传文件");
        }
        try {
            String result = multiFormatService.processAndSummarize(null, file);
            return Result.<String>success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
