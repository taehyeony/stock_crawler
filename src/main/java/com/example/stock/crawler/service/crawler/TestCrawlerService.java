package com.example.stock.crawler.service.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCrawlerService implements CommandLineRunner {

    private final StockCrawlerService stockCrawlerService;
    private final KospiIndexCrawlerService kospiIndexCrawlerService;
    private final OilCrawlerService oilCrawlerService;

    @Override
    public void run(String... args) throws Exception {
//        stockCrawlerService.getStockInfo();
//        stockCrawlerService.getStockPriceByDate(LocalDate.of(2025,03,06));
        List<LocalDate> dateList = new ArrayList<>();

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.now().minusDays(1);

        while (!startDate.isAfter(endDate)) {
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
        }

        for(LocalDate date: dateList){
            oilCrawlerService.getKospiByDate(date);
//            kospiIndexCrawlerService.getKospiByDate(date);
//            stockCrawlerService.getStockPriceByDate(date);
        }
    }
}
