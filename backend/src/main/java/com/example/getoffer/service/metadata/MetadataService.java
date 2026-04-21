package com.example.getoffer.service.metadata;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.dto.metadata.NameCountResponse;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.ArticleTagRepository;

@Service
public class MetadataService {

    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;

    public MetadataService(ArticleRepository articleRepository, ArticleTagRepository articleTagRepository) {
        this.articleRepository = articleRepository;
        this.articleTagRepository = articleTagRepository;
    }

    @Transactional(readOnly = true)
    public List<NameCountResponse> getCategories() {
        return articleRepository.findCategoryCounts().stream()
                .map(item -> new NameCountResponse(item.getName(), item.getCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NameCountResponse> getHotTags(int limit) {
        return articleTagRepository.findHotTags(PageRequest.of(0, limit)).stream()
                .map(item -> new NameCountResponse(item.getName(), item.getCount()))
                .toList();
    }
}
