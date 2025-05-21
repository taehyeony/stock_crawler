package com.example.stock.crawler.service.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CrawlerScheduler {
    private final TotalCrawlerService totalCrawlerService;

    @Scheduled(cron = "0 30 17 * * *")  // 매일 17:30 실행
    public void crawlingDaily() {
        totalCrawlerService.totalCrawling(LocalDate.now());
    }
}
