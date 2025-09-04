package com.ohnew.ohnew.service;

import com.ohnew.ohnew.converter.ChatHistoryConverter;
import com.ohnew.ohnew.converter.NewsArticleConverter;
import com.ohnew.ohnew.dto.req.ChatReq;
import com.ohnew.ohnew.dto.req.NewsArticleReq;
import com.ohnew.ohnew.dto.res.ChatRes;
import com.ohnew.ohnew.dto.res.NewsArticleRes;
import com.ohnew.ohnew.entity.ChatHistory;
import com.ohnew.ohnew.entity.NewsArticle;
import com.ohnew.ohnew.repository.ChatHistoryRepository;
import com.ohnew.ohnew.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final RssService rssService;
    private final PythonApiService pythonApiService;

    /**
     * RSS에서 뉴스를 가져와서 AI 처리 후 저장하는 메인 메소드
     */
    @Async
    public CompletableFuture<Integer> processRssNewsWithAI() {
        try {
            log.info("RSS 뉴스 처리 및 AI 분석 시작");

            // 1. RSS에서 뉴스 데이터 가져오기
            List<NewsArticleReq> rssNews = rssService.fetchNewsFromRss();
            if (rssNews.isEmpty()) {
                log.warn("RSS에서 가져온 뉴스가 없습니다.");
                return CompletableFuture.completedFuture(0);
            }

            log.info("RSS에서 {}개의 뉴스 기사 수집", rssNews.size());

            // 2. 중복 제거 (이미 저장된 기사는 제외)
            List<NewsArticleReq> newArticles = filterNewArticles(rssNews);
            if (newArticles.isEmpty()) {
                log.info("새로운 기사가 없습니다.");
                return CompletableFuture.completedFuture(0);
            }

            log.info("{}개의 새로운 기사 발견", newArticles.size());

            // 3. Python AI API로 뉴스 처리
            List<NewsArticleRes.NewsArticleDetail> processedNews = pythonApiService.processNewsWithAI(newArticles);
            if (processedNews.isEmpty()) {
                log.warn("AI 처리된 뉴스가 없습니다.");
                return CompletableFuture.completedFuture(0);
            }

            log.info("AI 처리 완료: {}개의 기사", processedNews.size());

            // 4. 데이터베이스에 저장
            int savedCount = saveProcessedNews(processedNews, rssNews);
            log.info("데이터베이스 저장 완료: {}개의 기사", savedCount);

            return CompletableFuture.completedFuture(savedCount);

        } catch (Exception e) {
            log.error("RSS 뉴스 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("RSS 뉴스 처리 실패", e);
        }
    }

    /**
     * 새로운 기사만 필터링 (중복 제거)
     */
    private List<NewsArticleReq> filterNewArticles(List<NewsArticleReq> rssNews) {
        return rssNews.stream()
                .filter(article -> {
                    // 기사 ID로 링크를 추정하거나, 제목 기반으로 중복 확인
                    String estimatedLink = "http://example.com/article/" + article.getArticleId();
                    return !newsArticleRepository.existsByLink(estimatedLink);
                })
                .toList();
    }

    /**
     * AI 처리된 뉴스를 데이터베이스에 저장
     */
    private int saveProcessedNews(List<NewsArticleRes.NewsArticleDetail> processedNews, List<NewsArticleReq> originalNews) {
        int savedCount = 0;
        
        // 원본 뉴스와 처리된 뉴스를 매핑
        Map<String, String> articleLinkMap = createArticleLinkMap(originalNews);

        for (NewsArticleRes.NewsArticleDetail processedArticle : processedNews) {
            try {
                String originalLink = articleLinkMap.get(processedArticle.getArticleId());
                
                NewsArticle newsArticle = NewsArticleConverter.toNewsArticleEntity(processedArticle, originalLink);
                
                // 중복 확인 후 저장
                if (!newsArticleRepository.existsByLink(newsArticle.getLink())) {
                    newsArticleRepository.save(newsArticle);
                    savedCount++;
                    log.debug("뉴스 기사 저장: {}", newsArticle.getTitle());
                } else {
                    log.debug("중복 기사 스킵: {}", newsArticle.getTitle());
                }
                
            } catch (Exception e) {
                log.error("뉴스 기사 저장 중 오류: {}", e.getMessage());
            }
        }

        return savedCount;
    }

    /**
     * 기사 ID와 링크 매핑 생성
     */
    private Map<String, String> createArticleLinkMap(List<NewsArticleReq> originalNews) {
        Map<String, String> linkMap = new HashMap<>();
        for (NewsArticleReq article : originalNews) {
            // 실제 링크가 있다면 사용하고, 없다면 가상 링크 생성
            String link = "http://example.com/article/" + article.getArticleId();
            linkMap.put(article.getArticleId(), link);
        }
        return linkMap;
    }

    /**
     * 뉴스 기사 상세 조회
     */
    @Transactional(readOnly = true)
    public NewsArticleRes.NewsArticleDetail getNewsArticleDetail(Long articleId) {
        NewsArticle newsArticle = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("뉴스 기사를 찾을 수 없습니다: " + articleId));
        
        return NewsArticleConverter.toNewsArticleDetail(newsArticle);
    }

    /**
     * 뉴스 기사 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public NewsArticleRes.NewsArticleListRes getNewsArticleList(Pageable pageable) {
        Page<NewsArticle> newsArticlePage = newsArticleRepository.findAllByOrderByCreatedAtDesc(pageable);
        return NewsArticleConverter.toNewsArticleListRes(newsArticlePage);
    }

    /**
     * 태그별 뉴스 기사 목록 조회
     */
    @Transactional(readOnly = true)
    public NewsArticleRes.NewsArticleListRes getNewsArticlesByTag(String tag, Pageable pageable) {
        Page<NewsArticle> newsArticlePage = newsArticleRepository.findByTag(tag, pageable);
        return NewsArticleConverter.toNewsArticleListRes(newsArticlePage);
    }

    /**
     * 키워드로 뉴스 기사 검색
     */
    @Transactional(readOnly = true)
    public NewsArticleRes.NewsArticleListRes searchNewsArticles(String keyword, Pageable pageable) {
        Page<NewsArticle> newsArticlePage = newsArticleRepository.searchByKeyword(keyword, pageable);
        return NewsArticleConverter.toNewsArticleListRes(newsArticlePage);
    }

    /**
     * 최근 뉴스 기사 요약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NewsArticleRes.NewsArticleSummary> getRecentNewsArticleSummaries(int limit) {
        List<NewsArticle> recentArticles = newsArticleRepository.findByCreatedAtAfter(
                LocalDateTime.now().minusDays(7)
        ).stream().limit(limit).toList();
        
        return NewsArticleConverter.toNewsArticleSummaryList(recentArticles);
    }

    /**
     * 태그별 기사 개수 통계
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getNewsCountByTag() {
        List<Object[]> results = newsArticleRepository.countByTag();
        Map<String, Long> tagCountMap = new HashMap<>();
        
        for (Object[] result : results) {
            String tag = (String) result[0];
            Long count = (Long) result[1];
            tagCountMap.put(tag, count);
        }
        
        return tagCountMap;
    }

    /**
     * Python API 서버 상태 확인
     */
    public boolean checkAIServiceHealth() {
        return pythonApiService.checkPythonApiHealth();
    }

    /**
     * 단일 뉴스 기사 AI 처리 및 저장
     */
    @Transactional
    public NewsArticleRes.NewsArticleDetail processSingleNewsArticle(NewsArticleReq newsArticleReq) {
        try {
            log.info("단일 뉴스 기사 AI 처리 시작: {}", newsArticleReq.getTitle());

            // 1. Python AI API로 단일 기사 처리
            NewsArticleRes.NewsArticleDetail processedArticle = pythonApiService.processSingleNewsWithAI(newsArticleReq);

            // 2. 중복 확인 후 저장
            String articleLink = "http://example.com/article/" + newsArticleReq.getArticleId();
            
            if (!newsArticleRepository.existsByLink(articleLink)) {
                NewsArticle newsArticle = NewsArticleConverter.toNewsArticleEntity(processedArticle, articleLink);
                newsArticleRepository.save(newsArticle);
                log.info("단일 뉴스 기사 저장 완료: {}", newsArticle.getTitle());
            } else {
                log.info("중복 기사로 저장 스킵: {}", processedArticle.getTitle());
            }

            return processedArticle;

        } catch (Exception e) {
            log.error("단일 뉴스 기사 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("단일 뉴스 기사 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 배치 뉴스 기사 AI 처리 및 저장
     */
    @Transactional
    public List<NewsArticleRes.NewsArticleDetail> processBatchNewsArticles(List<NewsArticleReq> newsArticleReqs) {
        try {
            log.info("배치 뉴스 기사 AI 처리 시작: {}개 기사", newsArticleReqs.size());

            // 1. 중복 제거 (이미 저장된 기사는 제외)
            List<NewsArticleReq> newArticles = filterNewArticles(newsArticleReqs);
            if (newArticles.isEmpty()) {
                log.info("새로운 기사가 없습니다.");
                return List.of();
            }

            // 2. Python AI API로 배치 기사 처리
            List<NewsArticleRes.NewsArticleDetail> processedArticles = pythonApiService.processNewsWithAI(newArticles);

            // 3. 데이터베이스에 저장
            int savedCount = saveProcessedNews(processedArticles, newArticles);
            log.info("배치 뉴스 기사 처리 완료: {}개 기사 저장", savedCount);

            return processedArticles;

        } catch (Exception e) {
            log.error("배치 뉴스 기사 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("배치 뉴스 기사 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 뉴스 기사에 대한 채팅 기능 (대화 내역 저장 포함)
     */
    @Transactional
    public ChatRes chatWithNewsArticle(Long articleId, Long userId, String userMessage) {
        try {
            log.info("뉴스 기사 채팅 요청: 기사 ID {}, 사용자 ID {}, 메시지: {}", articleId, userId, userMessage);

            // 1. 기사 존재 확인 및 요약 정보 가져오기
            NewsArticle newsArticle = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("뉴스 기사를 찾을 수 없습니다: " + articleId));

            // 2. 기존 대화 내역 조회 (최근 10개 대화만)
            List<ChatHistory> recentHistory = chatHistoryRepository
                    .findByArticleIdAndUserIdOrderByCreatedAtAsc(articleId, userId)
                    .stream()
                    .skip(Math.max(0, chatHistoryRepository.findByArticleIdAndUserIdOrderByCreatedAtAsc(articleId, userId).size() - 10))
                    .toList();

            // 3. 대화 내역을 DTO로 변환
            List<ChatReq.ChatHistory> historyDtos = ChatHistoryConverter.toChatHistoryDtoList(recentHistory);

            // 4. 채팅 요청 생성
            ChatReq chatRequest = ChatReq.builder()
                    .articleId(String.valueOf(articleId))
                    .userId(String.valueOf(userId))
                    .summary(newsArticle.getSummary())
                    .history(historyDtos)
                    .userMessage(userMessage)
                    .build();

            // 5. 세션 ID 생성 (같은 대화 세션 구분용)
            String sessionId = UUID.randomUUID().toString();

            // 6. 사용자 메시지 저장
            ChatHistory userChatHistory = ChatHistoryConverter.toUserChatHistoryEntity(
                    articleId, userId, userMessage, sessionId);
            chatHistoryRepository.save(userChatHistory);

            // 7. Python AI API로 채팅 처리
            ChatRes chatResponse = pythonApiService.chatWithArticle(chatRequest);

            // 8. AI 응답 저장 (성공한 경우에만)
            if (chatResponse.isSuccess() && chatResponse.getAnswer() != null) {
                ChatHistory assistantChatHistory = ChatHistoryConverter.toAssistantChatHistoryEntity(
                        articleId, userId, chatResponse.getAnswer(), sessionId);
                chatHistoryRepository.save(assistantChatHistory);
            }

            log.info("뉴스 기사 채팅 응답 완료: {}", chatResponse.getAnswer());
            return chatResponse;

        } catch (Exception e) {
            log.error("뉴스 기사 채팅 중 오류: {}", e.getMessage(), e);
            return ChatRes.builder()
                    .success(false)
                    .message("채팅 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .articleId(String.valueOf(articleId))
                    .build();
        }
    }

    /**
     * 특정 사용자와 기사의 대화 내역 조회
     */
    @Transactional(readOnly = true)
    public List<ChatReq.ChatHistory> getChatHistory(Long articleId, Long userId) {
        List<ChatHistory> chatHistories = chatHistoryRepository.findByArticleIdAndUserIdOrderByCreatedAtAsc(articleId, userId);
        return ChatHistoryConverter.toChatHistoryDtoList(chatHistories);
    }
}
