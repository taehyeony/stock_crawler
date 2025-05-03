package com.example.stock.crawler.cache;

import com.example.stock.crawler.entity.StockInfoEntity;
import com.example.stock.crawler.repository.StockInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StockInfoCache {
    // 주식 시세 데이터를 캐시할 ConcurrentHashMap
    private final ConcurrentHashMap<String, StockInfoEntity> stockInfoCache = new ConcurrentHashMap<>();
    private final StockInfoRepository stockInfoRepository;

    @PostConstruct
    public void loadCacheFromDatabase() {
        List<StockInfoEntity> allStocks = stockInfoRepository.findAll();
        for (StockInfoEntity stock : allStocks) {
            stockInfoCache.put(stock.getShortCode(), stock);
        }
        System.out.println("캐시 초기화 완료: " + stockInfoCache.size() + "건");
    }

    // 주식 시세를 캐시에서 가져오거나, 없으면 크롤링하여 저장
    public StockInfoEntity getStockInfo(String shortCode) {
        StockInfoEntity cachedStockInfo = stockInfoCache.get(shortCode);

        if (cachedStockInfo != null) {
            return cachedStockInfo;  // 캐시에서 바로 반환
        }

        // 캐시에 없으면 크롤링하여 DB에 저장 후 캐시
        StockInfoEntity newPrice = crawlAndSaveStockInfo(shortCode);
        stockInfoCache.put(shortCode, newPrice);  // 캐시 저장
        return newPrice;
    }

    // 크롤링하여 주식 시세 데이터를 가져오는 메소드 (간단한 예시)
    private StockInfoEntity crawlAndSaveStockInfo(String shortCode) {
        // 실제 크롤링 로직을 구현하고, 데이터베이스에 저장 후 결과 반환
        return stockInfoRepository.findByShortCode(shortCode);
    }
}

