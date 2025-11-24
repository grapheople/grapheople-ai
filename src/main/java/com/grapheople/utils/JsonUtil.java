package com.grapheople.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;

public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static ObjectWriter prettyWriter() {
        return objectMapper.writer().withDefaultPrettyPrinter();
    }

    public static ObjectWriter writer() {
        return objectMapper.writer();
    }

    public static <T> String toJsonString(T model) {
        ObjectWriter ow = writer();
        try {
            return ow.writeValueAsString(model);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static <T> String toPrettyJsonString(T model) {
        ObjectWriter ow = prettyWriter();
        try {
            return ow.writeValueAsString(model);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static JsonNode parse(Map map) {
        StringWriter stringify = new StringWriter();
        ObjectNode objectNode = null;
        try {
            objectMapper.writeValue(stringify, map);
            objectNode = (ObjectNode) objectMapper.readTree(stringify.toString());
        } catch (Exception e) {
            log.error("", e);
        }
        return objectNode;
    }

    public static JsonNode parse(String string) {
        try {
            return objectMapper.readTree(string);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    public static <T> T jsonStringToObject(String json, TypeReference<T> typeReference) {
        if (Strings.isNullOrEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("", e);
            log.error("json parsing fail. {}", e.getLocalizedMessage());
        }
        return null;
    }

    public static <T> T jsonStringToObject(String json, Class<T> cls) {
        if (Strings.isNullOrEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, cls);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static <T> T jsonToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
