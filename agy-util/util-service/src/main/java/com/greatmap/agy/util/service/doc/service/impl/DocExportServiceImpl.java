package com.greatmap.agy.util.service.doc.service.impl;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.doc.ModelAttr;
import com.greatmap.agy.util.api.dto.doc.Request;
import com.greatmap.agy.util.api.dto.doc.Response;
import com.greatmap.agy.util.api.dto.doc.Table;
import com.greatmap.agy.util.service.doc.service.DocExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Swagger / OpenAPI 接口文档导出服务实现
 *
 * @author AGY
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
@Service
@RequiredArgsConstructor
public class DocExportServiceImpl implements DocExportService {

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> parseFromUrl(String swaggerUrl) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String jsonStr = HttpUtil.get(swaggerUrl, 15000);
            resultMap = parseFromJsonString(jsonStr);
        } catch (Exception e) {
            log.error("根据 URL 解析 Swagger 接口异常：{}", swaggerUrl, e);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> parseFromJsonString(String jsonStr) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Table> result = new ArrayList<>();
        try {
            Map<String, Object> map = getResultFromString(result, jsonStr);
            Map<String, List<Table>> tableMap = result.stream()
                    .collect(Collectors.groupingBy(t -> StringUtils.defaultIfBlank(t.getTitle(), "默认分组")));
            resultMap.put("tableMap", new TreeMap<>(tableMap));
            resultMap.put("info", map.get("info"));
        } catch (Exception e) {
            log.error("解析 Swagger JSON 异常", e);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> parseFromFile(MultipartFile jsonFile) {
        try {
            String jsonStr = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);
            return parseFromJsonString(jsonStr);
        } catch (Exception e) {
            log.error("解析 Swagger 上传文件异常", e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> getResultFromString(List<Table> result, String jsonStr) throws IOException {
        Map<String, Object> map = objectMapper.readValue(jsonStr, new TypeReference<HashMap<String, Object>>() {});
        Map<String, ModelAttr> definitionMap = parseDefinitions(map);

        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
        if (paths != null) {
            for (Entry<String, Map<String, Object>> pathEntry : paths.entrySet()) {
                String url = pathEntry.getKey();
                Map<String, Object> methodMap = pathEntry.getValue();

                for (Entry<String, Object> requestEntry : methodMap.entrySet()) {
                    String requestType = requestEntry.getKey();
                    if ("parameters".equalsIgnoreCase(requestType)) {
                        continue;
                    }

                    Map<String, Object> content = (Map<String, Object>) requestEntry.getValue();

                    String title = "通用接口";
                    Object tagsObj = content.get("tags");
                    if (tagsObj instanceof List && !((List) tagsObj).isEmpty()) {
                        title = String.valueOf(((List) tagsObj).get(0));
                    }

                    String tag = String.valueOf(content.getOrDefault("summary", content.getOrDefault("operationId", "")));
                    String description = String.valueOf(content.getOrDefault("description", tag));

                    String requestForm = "";
                    List<String> consumes = (List<String>) content.get("consumes");
                    if (consumes != null && !consumes.isEmpty()) {
                        requestForm = String.join(",", consumes);
                    }

                    String responseForm = "";
                    List<String> produces = (List<String>) content.get("produces");
                    if (produces != null && !produces.isEmpty()) {
                        responseForm = String.join(",", produces);
                    }

                    List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");
                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");

                    Table table = new Table();
                    table.setTitle(title);
                    table.setUrl(url);
                    table.setTag(tag);
                    table.setDescription(description);
                    table.setRequestForm(requestForm);
                    table.setResponseForm(responseForm);
                    table.setRequestType(requestType.toUpperCase());
                    table.setRequestList(processRequestList(parameters, definitionMap));
                    if (responses != null) {
                        table.setResponseList(processResponseCodeList(responses));
                        Map<String, Object> obj200 = (Map<String, Object>) responses.get("200");
                        if (obj200 != null && obj200.get("schema") != null) {
                            table.setModelAttr(processResponseModelAttrs(obj200, definitionMap));
                        }
                        table.setResponseParam(processResponseParam(obj200, definitionMap));
                    }
                    table.setRequestParam(processRequestParam(table.getRequestList()));

                    result.add(table);
                }
            }
        }
        return map;
    }

    private List<Request> processRequestList(List<LinkedHashMap> parameters, Map<String, ModelAttr> definitionMap) {
        List<Request> requestList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Map<String, Object> param : parameters) {
                Object in = param.get("in");
                Request request = new Request();
                request.setName(String.valueOf(param.get("name")));
                request.setType(param.get("type") == null ? "object" : param.get("type").toString());
                if (param.get("format") != null) {
                    request.setType(request.getType() + "(" + param.get("format") + ")");
                }
                request.setParamType(String.valueOf(in));
                if ("body".equals(in)) {
                    Map<String, Object> schema = (Map) param.get("schema");
                    if (schema != null) {
                        Object ref = schema.get("$ref");
                        if ("array".equals(schema.get("type")) && schema.get("items") != null) {
                            ref = ((Map) schema.get("items")).get("$ref");
                            request.setType("array");
                        }
                        if (ref != null) {
                            request.setType(request.getType() + ":" + ref.toString().replaceAll("#/definitions/", ""));
                            request.setModelAttr(definitionMap.get(ref));
                        }
                    }
                }
                request.setRequire(Boolean.TRUE.equals(param.get("required")));
                request.setRemark(param.get("description") != null ? String.valueOf(param.get("description")) : "");
                requestList.add(request);
            }
        }
        return requestList;
    }

    private List<Response> processResponseCodeList(Map<String, Object> responses) {
        List<Response> responseList = new ArrayList<>();
        for (Entry<String, Object> entry : responses.entrySet()) {
            Response response = new Response();
            response.setName(entry.getKey());
            if (entry.getValue() instanceof Map) {
                Map<String, Object> statusCodeInfo = (Map<String, Object>) entry.getValue();
                response.setDescription(String.valueOf(statusCodeInfo.getOrDefault("description", "")));
                Object schema = statusCodeInfo.get("schema");
                if (schema instanceof Map) {
                    Object originalRef = ((Map) schema).get("originalRef");
                    response.setRemark(originalRef == null ? "" : originalRef.toString());
                }
            }
            responseList.add(response);
        }
        return responseList;
    }

    private ModelAttr processResponseModelAttrs(Map<String, Object> responseObj, Map<String, ModelAttr> definitionMap) {
        Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
        if (schema == null) {
            return new ModelAttr();
        }
        String type = (String) schema.get("type");
        String ref = null;
        if ("array".equals(type) && schema.get("items") instanceof Map) {
            ref = (String) ((Map) schema.get("items")).get("$ref");
        }
        if (schema.get("$ref") != null) {
            ref = (String) schema.get("$ref");
        }

        ModelAttr modelAttr = new ModelAttr();
        modelAttr.setType(StringUtils.defaultIfBlank(type, StringUtils.EMPTY));
        if (StringUtils.isNotBlank(ref) && definitionMap.get(ref) != null) {
            modelAttr = definitionMap.get(ref);
        }
        return modelAttr;
    }

    private Map<String, ModelAttr> parseDefinitions(Map<String, Object> map) {
        Map<String, Map<String, Object>> definitions = (Map<String, Map<String, Object>>) map.get("definitions");
        if (definitions == null && map.get("components") instanceof Map) {
            definitions = (Map<String, Map<String, Object>>) ((Map) map.get("components")).get("schemas");
        }
        Map<String, ModelAttr> definitionMap = new HashMap<>(256);
        if (definitions != null) {
            for (String modeName : definitions.keySet()) {
                getAndPutModelAttr(definitions, definitionMap, modeName);
            }
        }
        return definitionMap;
    }

    private ModelAttr getAndPutModelAttr(Map<String, Map<String, Object>> swaggerMap, Map<String, ModelAttr> resMap,
                                         String modeName) {
        String key = "#/definitions/" + modeName;
        ModelAttr modeAttr = resMap.computeIfAbsent(key, k -> new ModelAttr());
        if (modeAttr.isCompleted()) {
            return modeAttr;
        }

        Map<String, Object> modeDef = swaggerMap.get(modeName);
        if (modeDef == null) {
            return null;
        }
        Map<String, Object> modeProperties = (Map<String, Object>) modeDef.get("properties");
        if (modeProperties == null) {
            return null;
        }

        List<ModelAttr> attrList = new ArrayList<>();
        for (Entry<String, Object> mEntry : modeProperties.entrySet()) {
            Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();
            ModelAttr child = new ModelAttr();
            child.setName(mEntry.getKey());
            child.setType((String) attrInfoMap.get("type"));
            if (attrInfoMap.get("format") != null) {
                child.setType(child.getType() + "(" + attrInfoMap.get("format") + ")");
            }
            child.setType(StringUtils.defaultIfBlank(child.getType(), "object"));

            Object ref = attrInfoMap.get("$ref");
            Object items = attrInfoMap.get("items");
            if (ref != null || (items instanceof Map && (ref = ((Map) items).get("$ref")) != null)) {
                String refName = ref.toString();
                String clsName = refName.contains("/") ? refName.substring(refName.lastIndexOf("/") + 1) : refName;
                modeAttr.setCompleted(true);
                ModelAttr refModel = getAndPutModelAttr(swaggerMap, resMap, clsName);
                if (refModel != null) {
                    child.setProperties(refModel.getProperties());
                }
                child.setType(child.getType() + ":" + clsName);
            }
            child.setDescription((String) attrInfoMap.get("description"));
            attrList.add(child);
        }

        Object title = modeDef.get("title");
        Object description = modeDef.get("description");
        modeAttr.setClassName(title == null ? "" : title.toString());
        modeAttr.setDescription(description == null ? "" : description.toString());
        modeAttr.setProperties(attrList);
        return modeAttr;
    }

    private String processResponseParam(Map<String, Object> responseObj, Map<String, ModelAttr> definitionMap)
            throws JsonProcessingException {
        if (responseObj != null && responseObj.get("schema") != null) {
            Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
            String type = (String) schema.get("type");
            String ref = (String) schema.get("$ref");
            if ("array".equals(type) && schema.get("items") instanceof Map) {
                ref = (String) ((Map) schema.get("items")).get("$ref");
            }
            if (StringUtils.isNotEmpty(ref)) {
                ModelAttr modelAttr = definitionMap.get(ref);
                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                    Map<String, Object> responseMap = new HashMap<>(8);
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        responseMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                    }
                    return objectMapper.writeValueAsString(responseMap);
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private String processRequestParam(List<Request> list) throws IOException {
        Map<String, Object> headerMap = new LinkedHashMap<>();
        Map<String, Object> queryMap = new LinkedHashMap<>();
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        if (list != null) {
            for (Request request : list) {
                String name = request.getName();
                String paramType = request.getParamType();
                Object value = getValue(request.getType(), request.getModelAttr());
                if ("header".equalsIgnoreCase(paramType)) {
                    headerMap.put(name, value);
                } else if ("query".equalsIgnoreCase(paramType)) {
                    queryMap.put(name, value);
                } else if ("body".equalsIgnoreCase(paramType)) {
                    jsonMap.put(name, value);
                }
            }
        }
        StringBuilder res = new StringBuilder();
        if (!queryMap.isEmpty()) {
            res.append(queryMap.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&")));
        }
        if (!headerMap.isEmpty()) {
            res.append(" ").append(headerMap.entrySet().stream()
                    .map(e -> "--header '" + e.getKey() + ":" + e.getValue() + "'")
                    .collect(Collectors.joining(" ")));
        }
        if (!jsonMap.isEmpty()) {
            res.append(" -d '").append(objectMapper.writeValueAsString(jsonMap)).append("'");
        }
        return res.toString();
    }

    private Object getValue(String type, ModelAttr modelAttr) {
        if (type == null) {
            return null;
        }
        int pos = type.indexOf(":");
        if (pos != -1) {
            type = type.substring(0, pos);
        }
        switch (type) {
            case "string":
                return "string";
            case "string(date-time)":
                return "2026-08-25 10:00:00";
            case "integer":
            case "integer(int64)":
            case "integer(int32)":
                return 0;
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "file":
                return "(binary)";
            case "array":
                List<Object> list = new ArrayList<>();
                Map<String, Object> map = new LinkedHashMap<>();
                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        map.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                    }
                }
                list.add(map);
                return list;
            case "object":
                Map<String, Object> objMap = new LinkedHashMap<>();
                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        objMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                    }
                }
                return objMap;
            default:
                return null;
        }
    }
}
