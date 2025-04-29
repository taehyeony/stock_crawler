package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.StockInfoEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfoEntity, String> {
    StockInfoEntity findByShortCode(String shortCode);
}
