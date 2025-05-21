package com.example.stock.crawler.repository;

import com.example.stock.crawler.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity,Integer> {
    Optional<ExchangeRateEntity> findByDateAndCurrencyCode(LocalDate date, String CurrencyCode);
}
