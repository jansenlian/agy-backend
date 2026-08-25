package com.greatmap.agy.aks.vo;

import com.greatmap.agy.aks.entity.DkhcSummary;
import com.greatmap.agy.aks.entity.HtxxSummary;
import com.greatmap.agy.aks.entity.Yj;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 预警穿透诊断结果封装对象
 */
@Data
@Builder
public class WarningDiagnosisVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 诊断执行时间
     */
    private String diagnoseTime;

    /**
     * 预警单基础信息
     */
    private Yj warningInfo;

    /**
     * 穿透关联的合同档案
     */
    private HtxxSummary linkedContract;

    /**
     * 穿透关联的地块现状
     */
    private DkhcSummary linkedParcel;

    /**
     * 风险评级(CRITICAL:极高危, HIGH:高危, MEDIUM:中危, LOW:低危)
     */
    private String riskLevel;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 违规/异常成因剖析
     */
    private String causeAnalysis;

    /**
     * 合规处置与整改指导建议
     */
    private String actionAdvice;
}
