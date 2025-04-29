package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.StockInfoEntity;
import com.example.stock.crawler.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPriceEntity, Long> {

    @Query(value = "SELECT stock_price_id FROM stock_price where short_code = :short_code and date = :date", nativeQuery = true)
    Long findStockPriceIdByShortCodeAndDate(
            @Param("short_code") String ShortCode,
            @Param("date") LocalDate date);
}
