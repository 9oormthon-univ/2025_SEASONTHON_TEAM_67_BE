package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.entity.News;
import com.ohnew.ohnew.entity.NewsSummaryVariant;
import com.ohnew.ohnew.entity.enums.NewsStyle;
import com.ohnew.ohnew.repository.NewsRepository;
import com.ohnew.ohnew.repository.NewsSummaryVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsVariantService {

    private final NewsRepository newsRepository;
    private final NewsSummaryVariantRepository variantRepository;

    @Transactional
    public void upsertVariant(Long newsId,
                              NewsStyle style,
                              String newTitle,
                              String summary,
                              String epiStimulationReduced,
                              String epiReason) {

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(
                        ErrorStatus.RESOURCE_NOT_FOUND));

        NewsSummaryVariant entity = variantRepository.findByNewsIdAndNewsStyle(newsId, style)
                .orElseGet(() -> NewsSummaryVariant.builder()
                        .news(news)
                        .newsStyle(style)
                        .build());

        entity.updateFromLLM(newTitle, summary, epiStimulationReduced, epiReason);

        // 더티체킹으로도 flush되지만 upsert 명시를 위함....
        variantRepository.save(entity);
    }
}
