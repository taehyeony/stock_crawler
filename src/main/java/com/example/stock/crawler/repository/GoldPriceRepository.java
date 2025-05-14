package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.GoldPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoldPriceRepository extends JpaRepository<GoldPriceEntity, Integer> {
}
