package com.greatmap.agy.dwg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cad")
public class CadProperties {

    /**
     * dwg2dxf可执行程序路径或名称
     */
    private String dwg2dxfPath = "dwg2dxf";

    /**
     * 转换超时时间(秒)
     */
    private int timeoutSeconds = 60;

    /**
     * 临时文件存放目录
     */
    private String tempDir = "./temp/cad";
}
