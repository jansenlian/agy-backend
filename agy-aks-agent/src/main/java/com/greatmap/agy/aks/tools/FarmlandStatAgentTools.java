package com.greatmap.agy.aks.tools;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.greatmap.agy.aks.constant.AksConstant;
import com.greatmap.agy.aks.service.FarmlandStatService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 领导驾驶舱与大盘宏观统计工具集(FarmlandStatAgentTools)
 * 面向大模型Function Call的薄门面层,核心业务统计逻辑下沉至FarmlandStatService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FarmlandStatAgentTools {

    private final FarmlandStatService farmlandStatService;

    @Tool("获取阿克苏地区或指定区县的耕地大盘宏观总览指标(统计各区县合同总数、承包总面积、涉及地块总数、地块核查面积与租金总额)")
    public String getDistrictMacroOverview(
            @P("区县代码,如阿克苏市652901,温宿县652922,库车市652902,查询全区填652900000000或留空") String areaCode) {

        log.info(">>> [FarmlandStatAgentTool触发] 获取宏观大盘统计 -> areaCode: {}", areaCode);
        List<Map<String, Object>> list = farmlandStatService.getDistrictMacroOverview(areaCode);

        JSONObject result = new JSONObject();
        result.set("areaCode", StrUtil.blankToDefault(areaCode, AksConstant.AKSDQCODE));
        result.set("statTime", DateUtil.now());
        result.set("districtOverviewList", list);
        result.set("summary", "已成功聚合阿克苏全区耕地合同规模、承包面积、地块与租金宏观数据.");

        return result.toStringPretty();
    }

    @Tool("统计全区或指定区县的粮菜作物种植结构分布与非粮化风险分析(汇总粮食作物如小麦/玉米、经济作物如棉花/特色林果及蔬菜大棚的面积和占比)")
    public String getPlantingAndCropStats(
            @P("区县代码,查询全区填652900000000或留空") String areaCode) {

        log.info(">>> [FarmlandStatAgentTool触发] 统计作物种植结构 -> areaCode: {}", areaCode);
        List<Map<String, Object>> cropStats = farmlandStatService.getPlantingAndCropStats(areaCode);

        JSONObject result = new JSONObject();
        result.set("areaCode", StrUtil.blankToDefault(areaCode, AksConstant.AKSDQCODE));
        result.set("statTime", DateUtil.now());
        result.set("cropStructureList", cropStats);
        result.set("summary", "已成功统计各作物类型种植地块数与面积规模,可用于粮菜种植结构与非粮化监管研判.");

        return result.toStringPretty();
    }

    @Tool("统计各区县土地流转租金与承包财务收缴概况(包含租金总额、亩均租金指导价分布、最高/最低租金区间)")
    public String getRentAndFinancialStats(
            @P("区县代码,查询全区填652900000000或留空") String areaCode) {

        log.info(">>> [FarmlandStatAgentTool触发] 统计租金财务状况 -> areaCode: {}", areaCode);
        List<Map<String, Object>> rentStats = farmlandStatService.getRentAndFinancialStats(areaCode);

        JSONObject result = new JSONObject();
        result.set("areaCode", StrUtil.blankToDefault(areaCode, AksConstant.AKSDQCODE));
        result.set("statTime", DateUtil.now());
        result.set("rentOverviewList", rentStats);
        result.set("summary", "已成功汇总各区县租金总额与亩均单价统计.");

        return result.toStringPretty();
    }

    @Tool("统计全区合同与地块的审核合规履约流转状态(统计审核通过、流程中待审核、审核驳回/待整改的合同数量与涉及面积)")
    public String getAuditStatusStats(
            @P("区县代码,查询全区填652900000000或留空") String areaCode) {

        log.info(">>> [FarmlandStatAgentTool触发] 统计审批流转状态 -> areaCode: {}", areaCode);
        List<Map<String, Object>> auditStats = farmlandStatService.getAuditStatusStats(areaCode);

        JSONObject result = new JSONObject();
        result.set("areaCode", StrUtil.blankToDefault(areaCode, AksConstant.AKSDQCODE));
        result.set("statTime", DateUtil.now());
        result.set("auditStatusList", auditStats);
        result.set("summary", "已成功汇总合同审批流转状态与合规履约情况.");

        return result.toStringPretty();
    }
}
