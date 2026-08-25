package com.greatmap.agy.common.dict;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 字典翻译 Jackson 序列化器 (支持动态字典翻译与默认映射)
 */
public class DictSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private String dictType;

    private static BiFunction<String, String, String> dictTranslator;

    // 内置常见系统状态字典，支持秒级开箱即用
    private static final Map<String, Map<String, String>> BUILTIN_DICTS = new ConcurrentHashMap<>();

    static {
        BUILTIN_DICTS.put("user_status", Map.of("1", "正常", "0", "停用"));
        BUILTIN_DICTS.put("sys_user_sex", Map.of("0", "男", "1", "女", "2", "未知"));
        BUILTIN_DICTS.put("sys_oper_type", Map.of(
                "0", "其它", "1", "新增", "2", "修改", "3", "删除", "4", "授权", "5", "导出", "6", "清空"
        ));
        BUILTIN_DICTS.put("sys_common_status", Map.of("1", "成功", "0", "失败"));
    }

    public static void setDictTranslator(BiFunction<String, String, String> translator) {
        dictTranslator = translator;
    }

    public DictSerializer() {
    }

    public DictSerializer(String dictType) {
        this.dictType = dictType;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value);
        if (value != null && dictType != null) {
            String fieldName = gen.getOutputContext().getCurrentName();
            if (fieldName != null) {
                String strVal = String.valueOf(value);
                String label = null;

                // 1. 优先调用外部动态字典翻译器 (如 Redis / 数据库字典表)
                if (dictTranslator != null) {
                    try {
                        label = dictTranslator.apply(dictType, strVal);
                    } catch (Exception ignored) {
                    }
                }

                // 2. 兜底内置字典
                if (label == null) {
                    Map<String, String> dictMap = BUILTIN_DICTS.get(dictType);
                    if (dictMap != null) {
                        label = dictMap.get(strVal);
                    }
                }

                // 3. 最终未匹配时如实输出原值
                if (label == null) {
                    label = strVal;
                }

                gen.writeStringField(fieldName + "Label", label);
            }
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Dict dict = property.getAnnotation(Dict.class);
            if (dict != null) {
                return new DictSerializer(dict.type());
            }
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
