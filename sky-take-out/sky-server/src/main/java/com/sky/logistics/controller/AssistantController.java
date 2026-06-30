package com.sky.logistics.controller;

import com.sky.logistics.common.ApiResponse;
import com.sky.logistics.common.PageResponse;
import com.sky.logistics.service.LogisticsStarterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Api(tags = "智慧物流-智能问答与知识库")
public class AssistantController {

    private final LogisticsStarterService starterService;

    public AssistantController(LogisticsStarterService starterService) {
        this.starterService = starterService;
    }

    @PostMapping("/assistant/chat")
    @ApiOperation("智能问答")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        return ApiResponse.success(starterService.assistantChat(request));
    }

    @GetMapping("/assistant/suggestions")
    @ApiOperation("获取问答建议")
    public ApiResponse<List<String>> suggestions(@RequestParam(required = false) String cargoId) {
        return ApiResponse.success(starterService.assistantSuggestions(cargoId));
    }

    @GetMapping("/assistant/sessions/{sessionId}/messages")
    @ApiOperation("获取会话消息")
    public ApiResponse<List<Map<String, Object>>> messages(@PathVariable String sessionId) {
        return ApiResponse.success(java.util.Collections.<Map<String, Object>>emptyList());
    }

    @PostMapping("/knowledge/documents")
    @ApiOperation("上传知识库文档")
    public ApiResponse<Map<String, Object>> upload(@RequestParam(required = false) MultipartFile file,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String category) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("documentId", "DOC-STARTER");
        data.put("title", title);
        data.put("category", category);
        data.put("status", "UPLOADED");
        return ApiResponse.success(data);
    }

    @PostMapping("/knowledge/documents/{documentId}/index")
    @ApiOperation("索引知识库文档")
    public ApiResponse<Map<String, Object>> index(@PathVariable String documentId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("documentId", documentId);
        data.put("status", "INDEXED");
        data.put("chunkCount", 0);
        return ApiResponse.success(data);
    }

    @GetMapping("/knowledge/documents")
    @ApiOperation("获取知识库文档列表")
    public ApiResponse<PageResponse<Map<String, Object>>> documents(@RequestParam(required = false) Integer page,
                                                                    @RequestParam(required = false) Integer size) {
        return ApiResponse.success(starterService.knowledgeDocuments(page, size));
    }

    @DeleteMapping("/knowledge/documents/{documentId}")
    @ApiOperation("删除知识库文档")
    public ApiResponse<Void> delete(@PathVariable String documentId) {
        return ApiResponse.success();
    }
}
