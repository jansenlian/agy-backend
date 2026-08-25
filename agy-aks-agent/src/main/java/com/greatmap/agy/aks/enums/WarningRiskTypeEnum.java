package com.greatmap.agy.aks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警风险评估与处置策略枚举
 */
@Getter
@AllArgsConstructor
public enum WarningRiskTypeEnum {

    HTDQ("01", "MEDIUM", "合同履约即将到期风险",
            "该合同已临近约定的终止期限,需提前开展续约审批或收回重新流转.",
            "1. 提前30日向承租人送达《合同期满履约提醒函》; 2. 组织村集体与农户进行现场地貌验收,确认无耕地破坏; 3. 按照程序重新挂牌或续签合同."),

    DKYC("02", "HIGH", "地块现状与台账面积/边界异常",
            "地块实地核查面积与合同登记面积偏差超标,或四至边界存在重叠争议.",
            "1. 派驻外业调查组携带RTK设备进行现场复测定界; 2. 比对遥感影像历史底图; 3. 重新核发地块核查确认单并更新系统矢量图斑."),

    SJFY("03", "HIGH", "租金收缴严重逾期/欠费风险",
            "承包方未按合同约定期限足额缴纳年度土地租金,存在集体资产流失风险.",
            "1. 下达《催缴租金限期清收通知书》; 2. 逾期未补缴的,村集体有权依据《农村土地承包法》解除流转合同并申请司法追偿."),

    RYXX("04", "MEDIUM", "承包责任主体信息异常",
            "承包主体或承租人身份变更未报备,或存在挂靠/转包不合规风险.",
            "1. 核实承包方户籍与实际耕种人身份证明; 2. 重新补签变更补充协议并备案."),

    FGLH("05", "CRITICAL", "耕地非粮化/非农化重大违规",
            "永久基本农田或高标准农田上存在擅自种植多年生林果、挖塘养鱼或搭建非农设施违规行为.",
            "1. 立即下达《责令限期整改通知书》,责令其30日内恢复耕种条件; 2. 暂停发放耕地地力保护补贴; 3. 拒不整改的移交自然资源与农业综合行政执法大队依法查处."),

    OTHER("DEFAULT", "LOW", "常规业务提示",
            "系统日常业务规则巡检触发的预警提醒.",
            "请业务主管人员核实该笔记录的档案资料并跟进处理.");

    private final String code;
    private final String riskLevel;
    private final String riskTitle;
    private final String defaultCause;
    private final String actionAdvice;

    public static WarningRiskTypeEnum getByCode(String code) {
        if (code == null) {
            return OTHER;
        }
        for (WarningRiskTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return OTHER;
    }
}
