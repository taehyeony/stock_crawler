package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPriceEntity, Long> {
}
