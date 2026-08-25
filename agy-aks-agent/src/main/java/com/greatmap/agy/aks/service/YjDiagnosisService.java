package com.greatmap.agy.aks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.agy.aks.entity.Yj;
import com.greatmap.agy.aks.vo.WarningDiagnosisVo;

import java.util.Map;

/**
 * 预警台账与因果穿透诊断业务服务接口
 */
public interface YjDiagnosisService {

    /**
     * 分页多维检索预警列表
     */
    Page<Yj> queryWarningPage(int pageNo, int pageSize, String areaCode, String type, String startTime, String endTime, String keyword);

    /**
     * 穿透诊断特定预警记录/合同/地块的成因并输出风险评级与整改建议
     */
    WarningDiagnosisVo diagnoseWarning(String warningId, String contractNo, String parcelNo);

    /**
     * 统计全区或指定区县预警宏观态势与分布排行
     */
    Map<String, Object> getWarningStatistics(String areaCode);
}
