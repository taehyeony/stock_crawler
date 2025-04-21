package com.example.stock.crawler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="stock_price")
@Table(name="stock_price",indexes = {
            @Index(name = "idx_price_volume_value", columnList = "closing_price, price_change_rate, trading_value")
        })
public class StockPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockPriceId;

    @ManyToOne
    @JoinColumn(name = "short_code", nullable = false, columnDefinition = "varchar(15)") //길이 15, 빈칸 비 허용
    private StockInfoEntity stockInfoEntity;

    @Column(nullable = false) // 빈칸 비 허용
    private int closingPrice;

    @Column(nullable = false) // 빈칸 비 허용
    private int priceChange;

    @Column(nullable = false,  precision = 5, scale = 2) // decimal(5,2) 매핑, 빈칸 비 허용
    private BigDecimal priceChangeRate;

    @Column(nullable = false) // 빈칸 비 허용
    private int openingPrice;

    @Column(nullable = false) // 빈칸 비 허용
    private int highestPrice;

    @Column(nullable = false) // 빈칸 비 허용
    private int lowestPrice;

    @Column(nullable = false)
    private Long tradingVolume;

    @Column(nullable = false)
    private Long tradingValue;

    @Column(nullable = false)
    private Long marketCap;

    @Column(nullable = false)
    private Long listedStockNum;
}
