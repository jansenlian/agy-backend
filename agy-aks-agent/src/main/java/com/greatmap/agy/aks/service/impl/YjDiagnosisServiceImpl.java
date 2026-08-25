package com.greatmap.agy.aks.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.agy.aks.constant.AksConstant;
import com.greatmap.agy.aks.entity.DkhcSummary;
import com.greatmap.agy.aks.entity.HtxxSummary;
import com.greatmap.agy.aks.entity.Yj;
import com.greatmap.agy.aks.enums.WarningRiskTypeEnum;
import com.greatmap.agy.aks.mapper.DkhcSummaryMapper;
import com.greatmap.agy.aks.mapper.HtxxSummaryMapper;
import com.greatmap.agy.aks.mapper.YjMapper;
import com.greatmap.agy.aks.service.YjDiagnosisService;
import com.greatmap.agy.aks.vo.WarningDiagnosisVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警台账与因果穿透诊断业务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YjDiagnosisServiceImpl implements YjDiagnosisService {

    private final YjMapper yjMapper;
    private final HtxxSummaryMapper htxxSummaryMapper;
    private final DkhcSummaryMapper dkhcSummaryMapper;

    @Override
    public Page<Yj> queryWarningPage(int pageNo, int pageSize, String areaCode, String type, String startTime, String endTime, String keyword) {
        int current = (pageNo < 1) ? 1 : pageNo;
        int size = (pageSize < 1 || pageSize > 20) ? 10 : pageSize;

        log.info("[YjDiagnosisService] 查询预警列表 -> areaCode: {}, type: {}, keyword: {}, page: {}/{}",
                areaCode, type, keyword, current, size);

        Page<Yj> page = new Page<>(current, size);
        return yjMapper.selectWarningPage(page, areaCode, type, startTime, endTime, keyword);
    }

    @Override
    public WarningDiagnosisVo diagnoseWarning(String warningId, String contractNo, String parcelNo) {
        log.info("[YjDiagnosisService] 开展预警因果穿透诊断 -> warningId: {}, contractNo: {}, parcelNo: {}",
                warningId, contractNo, parcelNo);

        Yj warning = null;
        if (StrUtil.isNotBlank(warningId)) {
            warning = yjMapper.selectById(warningId.trim());
        }

        String htbh = StrUtil.isNotBlank(contractNo) ? contractNo.trim() : (warning != null ? warning.getHtbh() : null);
        String dkbh = StrUtil.isNotBlank(parcelNo) ? parcelNo.trim() : (warning != null ? warning.getDkbh() : null);

        HtxxSummary contract = StrUtil.isNotBlank(htbh) ? htxxSummaryMapper.findByHtbh(htbh) : null;
        DkhcSummary parcel = StrUtil.isNotBlank(dkbh) ? dkhcSummaryMapper.findByDkbh(dkbh) : null;

        // 根据预警类型匹配策略枚举
        String wType = (warning != null) ? warning.getType() : null;
        WarningRiskTypeEnum strategy = WarningRiskTypeEnum.getByCode(wType);

        String cause = strategy.getDefaultCause();
        if (warning != null && StrUtil.isNotBlank(warning.getXq())) {
            cause = warning.getXq() + " (成因分析: " + strategy.getDefaultCause() + ")";
        }

        return WarningDiagnosisVo.builder()
                .diagnoseTime(DateUtil.now())
                .warningInfo(warning)
                .linkedContract(contract)
                .linkedParcel(parcel)
                .riskLevel(strategy.getRiskLevel())
                .riskType(strategy.getRiskTitle())
                .causeAnalysis(cause)
                .actionAdvice(strategy.getActionAdvice())
                .build();
    }

    @Override
    public Map<String, Object> getWarningStatistics(String areaCode) {
        log.info("[YjDiagnosisService] 统计预警宏观态势 -> areaCode: {}", areaCode);

        List<Map<String, Object>> typeStats = yjMapper.countWarningGroupByType(areaCode);
        List<Map<String, Object>> districtStats = yjMapper.countWarningGroupByDistrict();

        Map<String, Object> result = new HashMap<>();
        result.put("areaCode", StrUtil.blankToDefault(areaCode, AksConstant.AKSDQCODE));
        result.put("statTime", DateUtil.now());
        result.put("warningTypeDistribution", typeStats);
        result.put("districtRanking", districtStats);

        return result;
    }
}
