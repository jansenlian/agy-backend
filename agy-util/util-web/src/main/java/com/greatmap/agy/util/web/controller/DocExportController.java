package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.util.service.doc.service.DocExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Swagger / OpenAPI 接口文档一键导出 Word 控制器
 *
 * @author AGY
 */
@Slf4j
@Controller
@RequestMapping("/doc")
@Tag(name = "Swagger文档导出", description = "Swagger / OpenAPI 接口文档一键转 Word (.docx)")
@RequiredArgsConstructor
public class DocExportController {

    private final DocExportService docExportService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${swagger.url:http://localhost/utilServer/v3/api-docs}")
    private String defaultSwaggerUrl;

    /**
     * 上传 Swagger JSON 文件并转换为 Word 文档下载
     */
    @Operation(summary = "上传 Swagger JSON 转换 Word 文档下载")
    @PostMapping(value = "/fileToWord", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void fileToWord(Model model,
                           @Parameter(description = "Swagger JSON 文件") @RequestPart("jsonFile") MultipartFile jsonFile,
                           HttpServletResponse response) {
        Map<String, Object> result = docExportService.parseFromFile(jsonFile);
        model.addAttribute("url", "http://");
        model.addAttribute("download", 0);
        model.addAllAttributes(result);
        downloadWord(model, response, "Swagger接口文档.docx");
    }

    /**
     * 根据在线 Swagger URL 一键下载为 Word 文档
     */
    @Operation(summary = "根据 Swagger URL 一键下载 Word 文档")
    @GetMapping(value = "/downloadWord")
    public void downloadWordFromUrl(Model model,
                                   @Parameter(description = "Swagger JSON 地址 (如 http://localhost:8080/v3/api-docs)")
                                   @RequestParam(required = false) String url,
                                   HttpServletResponse response) {
        String targetUrl = StringUtils.defaultIfBlank(url, defaultSwaggerUrl);
        Map<String, Object> result = docExportService.parseFromUrl(targetUrl);
        model.addAttribute("url", targetUrl);
        model.addAttribute("download", 0);
        model.addAllAttributes(result);
        downloadWord(model, response, "接口文档.docx");
    }

    private void downloadWord(Model model, HttpServletResponse response, String defaultFileName) {
        Context context = new Context();
        context.setVariables(model.asMap());
        String content = springTemplateEngine.process("word", context);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(defaultFileName, StandardCharsets.UTF_8));
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            // 写入 UTF-8 BOM 头，确保 Microsoft Word 正确以 UTF-8 打开中文
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            bos.write(bom, 0, bom.length);
            bos.write(bytes, 0, bytes.length);
            bos.flush();
        } catch (IOException e) {
            log.error("下载 Word 文档失败", e);
        }
    }
}
