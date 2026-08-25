package com.greatmap.agy.aks.service.impl;

import com.greatmap.agy.aks.mapper.FarmlandStatMapper;
import com.greatmap.agy.aks.service.FarmlandStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 领导驾驶舱与大盘宏观统计业务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FarmlandStatServiceImpl implements FarmlandStatService {

    private final FarmlandStatMapper farmlandStatMapper;

    @Override
    public List<Map<String, Object>> getDistrictMacroOverview(String areaCode) {
        log.info("[FarmlandStatService] 聚合各区县宏观大盘 -> areaCode: {}", areaCode);
        return farmlandStatMapper.countDistrictMacroOverview(areaCode);
    }

    @Override
    public List<Map<String, Object>> getPlantingAndCropStats(String areaCode) {
        log.info("[FarmlandStatService] 统计粮菜作物种植结构 -> areaCode: {}", areaCode);
        return farmlandStatMapper.countCropPlantingStructure(areaCode);
    }

    @Override
    public List<Map<String, Object>> getRentAndFinancialStats(String areaCode) {
        log.info("[FarmlandStatService] 统计租金财务收缴概况 -> areaCode: {}", areaCode);
        return farmlandStatMapper.countRentAndFinancialOverview(areaCode);
    }

    @Override
    public List<Map<String, Object>> getAuditStatusStats(String areaCode) {
        log.info("[FarmlandStatService] 统计合同与地块审批流转状态 -> areaCode: {}", areaCode);
        return farmlandStatMapper.countContractAuditStatus(areaCode);
    }
}
