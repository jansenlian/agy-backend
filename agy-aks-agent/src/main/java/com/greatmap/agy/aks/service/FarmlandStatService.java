package com.greatmap.agy.aks.service;

import java.util.List;
import java.util.Map;

/**
 * 领导驾驶舱与大盘宏观统计业务服务接口
 */
public interface FarmlandStatService {

    /**
     * 获取全区或指定区县宏观大盘总览
     */
    List<Map<String, Object>> getDistrictMacroOverview(String areaCode);

    /**
     * 获取粮菜作物种植结构分布与非粮化分析
     */
    List<Map<String, Object>> getPlantingAndCropStats(String areaCode);

    /**
     * 获取各区县租金与财务收缴概况
     */
    List<Map<String, Object>> getRentAndFinancialStats(String areaCode);

    /**
     * 获取合同审批流转状态与合规履约情况
     */
    List<Map<String, Object>> getAuditStatusStats(String areaCode);
}
