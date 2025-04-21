package com.example.stock.crawler.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestCrawlerService implements CommandLineRunner {

    private final StockCrawlerService stockCrawlerService;

    @Override
    public void run(String... args) throws Exception {
        stockCrawlerService.getStockInfo();
    }
}
