package com.greatmap.agy.dwg.service.converter;

import com.greatmap.agy.dwg.config.CadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibreDwgConverter {

    private final CadProperties cadProperties;

    /**
     * 将DWG文件转换为DXF文件
     *
     * @param dwgFile 输入DWG文件
     * @param dxfFile 输出DXF文件
     * @return 是否转换成功
     */
    public boolean convertDwgToDxf(File dwgFile, File dxfFile) {
        if (dwgFile == null || !dwgFile.exists() || dwgFile.length() == 0) {
            log.error("DWG文件不存在或为空: {}", dwgFile);
            return false;
        }

        String command = resolveExecutablePath();
        log.info("开始调用转换工具[{}]执行DWG->DXF转码: {}", command, dwgFile.getName());

        try {
            // 命令: dwg2dxf -y -v 2018 input.dwg -o output.dxf
            ProcessBuilder pb = new ProcessBuilder(
                    command,
                    "-y",
                    dwgFile.getAbsolutePath(),
                    "-o",
                    dxfFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            // 异步消费进程输出避免缓冲区死锁
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            boolean finished = process.waitFor(cadProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            long cost = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                log.error("DWG转码超时(已终止进程,耗时{}ms): {}", cost, dwgFile.getName());
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0 || !dxfFile.exists() || dxfFile.length() == 0) {
                log.error("DWG转码失败, exitCode={}, 耗时{}ms, 工具输出:\n{}", exitCode, cost, output);
                return false;
            }

            log.info("DWG转码成功, DXF文件大小: {}KB, 耗时{}ms", dxfFile.length() / 1024, cost);
            return true;
        } catch (Exception e) {
            log.error("调用dwg2dxf执行转码发生异常: ", e);
            return false;
        }
    }

    /**
     * 检查本地dwg2dxf命令是否可用
     */
    public boolean isAvailable() {
        try {
            String command = resolveExecutablePath();
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取dwg2dxf工具的具体版本信息(例如 dwg2dxf 0.13.3)
     */
    public String getToolVersion() {
        try {
            String command = resolveExecutablePath();
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() > 0) {
                        output.append(" ");
                    }
                    output.append(line.trim());
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0 && output.length() > 0) {
                return output.toString().trim();
            }
        } catch (Exception e) {
            log.debug("获取dwg2dxf版本异常: {}", e.getMessage());
        }
        return "Unknown";
    }

    /**
     * 跨平台自动探测dwg2dxf路径(Mac/Linux/Windows)
     */
    public String resolveExecutablePath() {
        String configured = cadProperties.getDwg2dxfPath();
        if (configured != null && !configured.trim().isEmpty() && new File(configured).exists()) {
            return configured;
        }

        // 常见环境路径探测
        String[] candidatePaths = {
                "/opt/homebrew/bin/dwg2dxf", // macOS Apple Silicon
                "/usr/local/bin/dwg2dxf",    // macOS Intel / Linux自定义编译
                "/usr/bin/dwg2dxf"           // Linux apt/yum
        };

        for (String candidate : candidatePaths) {
            if (new File(candidate).exists()) {
                return candidate;
            }
        }

        return configured != null && !configured.trim().isEmpty() ? configured : "dwg2dxf";
    }
}
