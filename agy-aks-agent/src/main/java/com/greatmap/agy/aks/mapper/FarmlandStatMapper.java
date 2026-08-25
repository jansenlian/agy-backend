package com.greatmap.agy.aks.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 领导驾驶舱与大盘宏观统计持久层接口
 */
@Mapper
public interface FarmlandStatMapper {

    /**
     * 各区县宏观大盘统计(合同数,承包总面积,地块数,地块核查面积,租金总额)
     */
    List<Map<String, Object>> countDistrictMacroOverview(@Param("areaCode") String areaCode);

    /**
     * 粮菜作物种植结构统计(按作物/种植类型统计地块数与总面积)
     */
    List<Map<String, Object>> countCropPlantingStructure(@Param("areaCode") String areaCode);

    /**
     * 租金与财务收缴统计(各区县租金总额,单价分布)
     */
    List<Map<String, Object>> countRentAndFinancialOverview(@Param("areaCode") String areaCode);

    /**
     * 合同审核与合规状态统计
     */
    List<Map<String, Object>> countContractAuditStatus(@Param("areaCode") String areaCode);
}
