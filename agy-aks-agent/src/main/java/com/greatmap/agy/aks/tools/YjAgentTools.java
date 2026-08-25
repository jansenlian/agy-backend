package com.greatmap.agy.aks.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.agy.aks.entity.Yj;
import com.greatmap.agy.aks.service.YjDiagnosisService;
import com.greatmap.agy.aks.vo.WarningDiagnosisVo;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 违法违规与风险预警诊断工具集(YjAgentTools)
 * 面向大模型Function Call的薄门面层,核心业务逻辑下沉至YjDiagnosisService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YjAgentTools {

    private final YjDiagnosisService yjDiagnosisService;

    @Tool("分页或多条件检索阿克苏智慧耕地违法违规与风险预警台账列表(支持按区县代码、预警类型01合同到期/02地块异常/03费用异常/04人员异常/05非粮化、起止时间及关键词模糊筛选)")
    public String queryWarningList(
            @P("区县代码,如阿克苏市652901,温宿县652922,库车市652902,不限或查询全区填652900000000或留空") String areaCode,
            @P("预警类型(01:合同到期预警, 02:地块异常预警, 03:收缴费用异常预警, 04:人员信息异常预警, 05:耕地非粮化预警, 可留空)") String type,
            @P("开始时间,格式yyyy-MM-dd,可留空") String startTime,
            @P("结束时间,格式yyyy-MM-dd,可留空") String endTime,
            @P("搜索关键词(地块号、合同号、预警标题或详情),可留空") String keyword,
            @P("页码,默认1") Integer pageNo,
            @P("每页条数,默认10,最大20") Integer pageSize) {

        int current = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int size = (pageSize == null || pageSize < 1 || pageSize > 20) ? 10 : pageSize;

        log.info(">>> [YjAgentTool触发] 检索预警台账 -> areaCode: {}, type: {}, keyword: {}", areaCode, type, keyword);
        Page<Yj> resultPage = yjDiagnosisService.queryWarningPage(current, size, areaCode, type, startTime, endTime, keyword);

        JSONObject response = new JSONObject();
        response.set("total", resultPage.getTotal());
        response.set("current", resultPage.getCurrent());
        response.set("pages", resultPage.getPages());
        response.set("records", resultPage.getRecords());

        return response.toStringPretty();
    }

    @Tool("穿透诊断特定预警记录/地块/合同的违法违规成因,并输出深度因果分析、涉案面积金额与合规处置整改建议")
    public String diagnoseWarningCase(
            @P("预警记录ID(可选)") String warningId,
            @P("合同编号(如HT6529..., 可选)") String contractNo,
            @P("地块编号(如DK6529..., 可选)") String parcelNo) {

        log.info(">>> [YjAgentTool触发] 预警穿透因果诊断 -> warningId: {}, contractNo: {}, parcelNo: {}",
                warningId, contractNo, parcelNo);

        WarningDiagnosisVo diagnosisResult = yjDiagnosisService.diagnoseWarning(warningId, contractNo, parcelNo);
        return JSONUtil.toJsonPrettyStr(diagnosisResult);
    }

    @Tool("统计全区或指定区县的违规预警宏观态势(包含预警类型分布、各区县预警高发排行与风险类型占比)")
    public String getWarningStatistics(
            @P("区县代码,查询全区请填652900000000或留空") String areaCode) {

        log.info(">>> [YjAgentTool触发] 预警大盘态势统计 -> areaCode: {}", areaCode);
        Map<String, Object> statResult = yjDiagnosisService.getWarningStatistics(areaCode);
        return JSONUtil.toJsonPrettyStr(statResult);
    }
}
