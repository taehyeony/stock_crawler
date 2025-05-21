package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.entity.CrawlInfoEntity;
import com.example.stock.crawler.repository.CrawlInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RunningService implements CommandLineRunner {

    private final TotalCrawlerService totalCrawlerService;
    private final CrawlInfoRepository crawlInfoRepository;


    @Override
    public void run(String... args) throws Exception {
        Optional<CrawlInfoEntity> optionalInfo = crawlInfoRepository.findById(1);
        CrawlInfoEntity crawlInfo = optionalInfo.orElseGet(() -> {
            CrawlInfoEntity dummy = new CrawlInfoEntity();
            dummy.setLastCrawledDate(LocalDate.of(2022,1,1));
            return crawlInfoRepository.save(dummy); // 저장 후 반환
        });

        LocalDate startDate = crawlInfo.getLastCrawledDate();
        LocalDate endDate = LocalDate.now().minusDays(1);

        while (!startDate.isAfter(endDate)) {
            // 크롤링 실행
            totalCrawlerService.totalCrawling(startDate);

            // 크롤링 완료되었으므로 날짜 업데이트
            startDate = startDate.plusDays(1);

            crawlInfo.setLastCrawledDate(startDate);
            crawlInfoRepository.save(crawlInfo);


        }
    }
}
