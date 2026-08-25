package com.greatmap.agy.common.desensitize;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 敏感数据脱敏 Jackson 序列化器 (支持手机号、身份证、邮箱、中文姓名脱敏，完善边界防越界保护)
 */
public class DataMaskSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DataMaskType maskType;

    public DataMaskSerializer() {
    }

    public DataMaskSerializer(DataMaskType maskType) {
        this.maskType = maskType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString("");
            return;
        }
        if (maskType == null) {
            gen.writeString(value);
            return;
        }

        switch (maskType) {
            case MOBILE, PHONE -> {
                if (value.length() >= 7) {
                    gen.writeString(StrUtil.hide(value, 3, 7));
                } else {
                    gen.writeString(value);
                }
            }
            case ID_CARD -> {
                if (value.length() >= 14) {
                    gen.writeString(StrUtil.hide(value, 4, 14));
                } else {
                    gen.writeString(value);
                }
            }
            case EMAIL -> {
                int atIdx = value.indexOf("@");
                if (atIdx > 1) {
                    gen.writeString(StrUtil.hide(value, 1, atIdx));
                } else {
                    gen.writeString(value);
                }
            }
            case CHINESE_NAME -> {
                if (value.length() <= 1) {
                    gen.writeString(value);
                } else if (value.length() == 2) {
                    gen.writeString(StrUtil.hide(value, 1, 2));
                } else {
                    gen.writeString(StrUtil.hide(value, 1, value.length() - 1));
                }
            }
            default -> gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            DataMask dataMask = property.getAnnotation(DataMask.class);
            if (dataMask != null) {
                return new DataMaskSerializer(dataMask.value());
            }
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
