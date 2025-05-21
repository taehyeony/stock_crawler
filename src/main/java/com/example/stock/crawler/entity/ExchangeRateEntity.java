package com.example.stock.crawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name="exchange_rate")
@Table(
        name="exchange_rate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"currency_code","date"})
)
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int exchangeId;

    @Column(nullable = false,length = 20)
    private String currencyCode;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "`change`", nullable = false,  precision = 10, scale = 2)
    private BigDecimal change;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal openingPrice;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal highestPrice;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal lowestPrice;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal closingPrice;

    @Column(nullable = false,  precision = 10, scale = 2)
    private BigDecimal tradingVolume;
}
