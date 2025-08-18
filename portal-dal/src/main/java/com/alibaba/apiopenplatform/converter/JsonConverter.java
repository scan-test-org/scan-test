package com.alibaba.apiopenplatform.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.support.common.Encrypted;
import com.alibaba.apiopenplatform.support.common.Encryptor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Field;

/**
 * @author zh
 */
@Slf4j
public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

    private final Class<T> type;

    protected JsonConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }

        T clonedAttribute = cloneAndEncrypt(attribute);
        return JSONUtil.toJsonStr(clonedAttribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        T attribute = JSONUtil.toBean(dbData, type);
        decrypt(attribute);
        return attribute;
    }

    private T cloneAndEncrypt(T original) {
        // Clone避免JPA更新数据
        T cloned = JSONUtil.toBean(JSONUtil.toJsonStr(original), type);
        handleEncryption(cloned, true);
        return cloned;
    }

    private void decrypt(T attribute) {
        handleEncryption(attribute, false);
    }

    private void handleEncryption(Object obj, boolean isEncrypt) {
        if (obj == null) {
            return;
        }

        BeanUtil.descForEach(obj.getClass(), pd -> {
            Field field = pd.getField();
            if (field == null) {
                return;
            }

            Object value = ReflectUtil.getFieldValue(obj, field);
            if (value == null) {
                return;
            }

            // 处理需要加密/解密的字段
            if (field.isAnnotationPresent(Encrypted.class) && value instanceof String) {
                String result = isEncrypt ?
                        Encryptor.encrypt((String) value) :
                        Encryptor.decrypt((String) value);
                ReflectUtil.setFieldValue(obj, field, result);
            } else if (!ClassUtil.isSimpleValueType(value.getClass())) {
                handleEncryption(value, isEncrypt);
            }
        });
    }
}
