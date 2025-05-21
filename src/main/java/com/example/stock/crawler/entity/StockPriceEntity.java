package com.example.stock.crawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name="stock_price")
@Table(
        name="stock_price",
        indexes = {
                @Index(name = "idx_price_volume_value", columnList = "closing_price, price_change_rate, trading_value")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"short_code","date"})

)
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

    @Column(nullable = false)
    private LocalDate date;

    @Override
    public String toString() {
        return "StockPriceEntity{" +
                "stockPriceId=" + stockPriceId +
                ", stockInfoEntity=" + (stockInfoEntity != null ? stockInfoEntity.getShortCode() : "null") +
                ", closingPrice=" + closingPrice +
                ", priceChange=" + priceChange +
                ", priceChangeRate=" + priceChangeRate +
                ", openingPrice=" + openingPrice +
                ", highestPrice=" + highestPrice +
                ", lowestPrice=" + lowestPrice +
                ", tradingVolume=" + tradingVolume +
                ", tradingValue=" + tradingValue +
                ", marketCap=" + marketCap +
                ", listedStockNum=" + listedStockNum +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPriceEntity stockPriceEntity = (StockPriceEntity) o;
        return Objects.equals(stockInfoEntity, stockPriceEntity.stockInfoEntity) && Objects.equals(date, stockPriceEntity.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockInfoEntity,date);
    }
}
