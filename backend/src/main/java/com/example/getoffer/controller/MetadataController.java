package com.example.getoffer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.dto.metadata.NameCountResponse;
import com.example.getoffer.service.metadata.MetadataService;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<NameCountResponse>> categories() {
        return ApiResponse.success(metadataService.getCategories());
    }

    @GetMapping("/tags/hot")
    public ApiResponse<List<NameCountResponse>> hotTags(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(metadataService.getHotTags(limit));
    }
}
