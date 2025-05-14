package com.example.stock.crawler.cache;

import com.example.stock.crawler.entity.StockInfoEntity;
import com.example.stock.crawler.entity.enumeration.CertificateType;
import com.example.stock.crawler.entity.enumeration.MarketType;
import com.example.stock.crawler.entity.enumeration.StockType;
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
    public StockInfoEntity getStockInfo(String shortCode,String stockName) {
        StockInfoEntity cachedStockInfo = stockInfoCache.get(shortCode);

        if (cachedStockInfo != null) {
            return cachedStockInfo;  // 캐시에서 바로 반환
        }

        // 캐시에 없으면 dummyData로 DB에 저장 후 캐시
        StockInfoEntity newPrice = crawlAndSaveStockInfo(shortCode,stockName);
        stockInfoCache.put(shortCode, newPrice);  // 캐시 저장
        return newPrice;
    }

    // http://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101
    // 위 페이지에 주식 정보가 없는 경우
    private StockInfoEntity crawlAndSaveStockInfo(String shortCode, String stockName) {
        StockInfoEntity tempStockInfoEntity = StockInfoEntity.builder()
                .shortCode(shortCode)
                .standardCode("KR----------")
                .korStockName(stockName)
                .korShortStockName(stockName)
                .engStockName(stockName)
                .listingDate(java.sql.Date.valueOf("2001-01-01"))
                .marketType(MarketType.KOSPI)
                .certificateType(CertificateType.주권)
                .department("")
                .stockType(StockType.보통주)
                .faceValue(0)
                .listedStockNum(0L)
                .build();
        StockInfoEntity stockInfoEntity = stockInfoRepository.save(tempStockInfoEntity);
        System.out.println(stockName + "(" + shortCode + ") 추가 등록");
        return stockInfoEntity;
    }
}

