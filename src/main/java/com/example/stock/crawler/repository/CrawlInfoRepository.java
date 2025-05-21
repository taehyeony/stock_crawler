package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.CrawlInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlInfoRepository extends JpaRepository<CrawlInfoEntity,Integer> {
}
