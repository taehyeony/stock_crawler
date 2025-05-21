package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.repository.CrawlInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TotalCrawlerService {
    private final StockCrawlerService stockCrawlerService;
    private final KospiIndexCrawlerService kospiIndexCrawlerService;
    private final OilCrawlerService oilCrawlerService;
    private final GoldCrawlerService goldCrawlerService;
    private final ExchangeRateCrawlerService exchangeRateCrawlerService;

    public void totalCrawling(LocalDate date){
        stockCrawlerService.getStockPriceByDate(date);
        kospiIndexCrawlerService.getKospiByDate(date);
        oilCrawlerService.getOilPriceByDate(date);
        goldCrawlerService.getGoldPriceByDate(date);
        exchangeRateCrawlerService.getExchangeRateByDate(date);
    }
}
