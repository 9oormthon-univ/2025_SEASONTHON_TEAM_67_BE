package com.ohnew.ohnew.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsSchedulerService {

    private final NewsArticleService newsArticleService;

    /**
     * 매시간 RSS 뉴스를 수집하고 AI 처리
     * cron: 매시간 정각에 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processRssNewsScheduled() {
        log.info("스케줄러: RSS 뉴스 처리 시작");
        
        try {
            newsArticleService.processRssNewsWithAI()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("스케줄러: RSS 뉴스 처리 중 오류 발생: {}", throwable.getMessage());
                    } else {
                        log.info("스케줄러: RSS 뉴스 처리 완료, {}개 기사 저장", result);
                    }
                });
                
        } catch (Exception e) {
            log.error("스케줄러: RSS 뉴스 처리 시작 실패: {}", e.getMessage());
        }
    }

    /**
     * 매일 자정에 AI 서비스 상태 확인
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkAIServiceHealth() {
        log.info("스케줄러: AI 서비스 상태 확인");
        
        try {
            boolean isHealthy = newsArticleService.checkAIServiceHealth();
            if (isHealthy) {
                log.info("스케줄러: AI 서비스 정상 동작 중");
            } else {
                log.warn("스케줄러: AI 서비스 상태 이상 감지");
            }
        } catch (Exception e) {
            log.error("스케줄러: AI 서비스 상태 확인 실패: {}", e.getMessage());
        }
    }
}
