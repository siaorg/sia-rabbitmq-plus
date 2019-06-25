package com.sia.rabbitmqplus.helpers;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * @author: pengfeili23@creditease.cn
 * @Description: JSON 与对象之间互转
 * @date: 2018年6月27日 下午5:48:06
 */
public class JSONHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JSONHelper() {
    }

    public static String toString(Object obj) {

        if (null == obj)
            return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {

            // ignore
        }
        return obj.toString();
    }

    public static <T> T toObject(String jsonString, Class<T> c) {

        if (null == c || StringHelper.isEmpty(jsonString)) {
            return null;
        }

        try {
            return MAPPER.readValue(jsonString, c);
        } catch (Exception e) {

            // ignore
        }
        return null;
    }

    public static <T> List<T> toObjectArray(String jsonString, Class<T> c) {

        if (null == c || StringHelper.isEmpty(jsonString)) {
            return Collections.emptyList();
        }
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, c);

            return MAPPER.readValue(jsonString, javaType);
        } catch (Exception e) {

            // ignore
        }
        return null;
    }
}