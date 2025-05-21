package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.entity.CrawlInfoEntity;
import com.example.stock.crawler.repository.CrawlInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestCrawlerService implements CommandLineRunner {

    private final StockCrawlerService stockCrawlerService;
    private final KospiIndexCrawlerService kospiIndexCrawlerService;
    private final OilCrawlerService oilCrawlerService;
    private final GoldCrawlerService goldCrawlerService;
    private final ExchangeRateCrawlerService exchangeRateCrawlerService;
    private final CrawlInfoRepository crawlInfoRepository;


    @Override
    public void run(String... args) throws Exception {
        Optional<CrawlInfoEntity> optionalInfo = crawlInfoRepository.findById(1);
        CrawlInfoEntity crawlInfo = optionalInfo.orElseThrow(() -> new RuntimeException("CrawlInfo with ID 1 not found"));

        LocalDate startDate = crawlInfo.getLastCrawledDate();
        LocalDate endDate = LocalDate.now().minusDays(1);

        while (!startDate.isAfter(endDate)) {
            // 크롤링 실행
            stockCrawlerService.getStockPriceByDate(startDate);
            kospiIndexCrawlerService.getKospiByDate(startDate);
            oilCrawlerService.getOilPriceByDate(startDate);
            goldCrawlerService.getGoldPriceByDate(startDate);
            exchangeRateCrawlerService.getExchangeRateByDate(startDate);

            // 크롤링 완료되었으므로 날짜 업데이트
            startDate = startDate.plusDays(1);

            crawlInfo.setLastCrawledDate(startDate);
            crawlInfoRepository.save(crawlInfo);


        }
    }
}
