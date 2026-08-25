package com.greatmap.agy.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 示例电商业务工具类
 * 只要标记了 @Component 和 @Tool，就会被 AgyAgentAutoConfiguration 自动发现并挂载到智能体大脑！
 */
@Component
public class EcommerceTools {

    @Tool("根据商品名称查询实时库存和单价")
    public String queryProductInfo(@P("商品名称，如 iPhone 16 Pro、华为 Mate 70") String productName) {
        System.out.println(">>> [Spring 业务组件触发] 正在查询数据库: " + productName);
        if (productName.toLowerCase().contains("iphone")) {
            return "【查询结果】商品: iPhone 16 Pro, 当前库存: 5 台, 官方单价: 7999.00 元";
        } else if (productName.contains("华为") || productName.toLowerCase().contains("mate")) {
            return "【查询结果】商品: 华为 Mate 70 Pro, 当前库存: 3 台, 官方单价: 6999.00 元";
        }
        return "【查询结果】商品: " + productName + ", 当前无库存";
    }

    @Tool("根据用户会员等级和原始总金额, 计算折扣后的实付价格")
    public String calculateDiscount(
            @P("用户会员等级，可选值: VIP、普通") String level,
            @P("原始订单总金额") double originalPrice) {
        System.out.println(">>> [Spring 业务组件触发] 计算会员折扣, 等级: " + level + ", 原价: " + originalPrice);
        double discount = "VIP".equalsIgnoreCase(level) ? 0.85 : 1.0;
        double finalPrice = originalPrice * discount;
        return String.format("【计算结果】会员等级: %s, 享受折扣: %.1f折, 最终实付金额: %.2f 元",
                level, (discount * 10), finalPrice);
    }
}
