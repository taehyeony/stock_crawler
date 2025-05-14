package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.KospiIndexEntity;
import com.example.stock.crawler.entity.StockInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KospiIndexRepository extends JpaRepository<KospiIndexEntity, Integer> {
}
