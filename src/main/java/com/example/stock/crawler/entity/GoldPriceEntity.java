package com.example.stock.crawler.entity;

import com.example.stock.crawler.entity.enumeration.GoldType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name="gold_price")
@Table(
        name="gold_price",
        uniqueConstraints = @UniqueConstraint(columnNames = {"gold_type","date"})
)
public class GoldPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int goldId;

    @Column(nullable = false)
    private int goldCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoldType goldType;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int closingPrice;

    @Column(nullable = false)
    private int goldChange;

    @Column(nullable = false,  precision = 5, scale = 2)
    private BigDecimal goldChangeRate;

    @Column(nullable = false)
    private int openingPrice;

    @Column(nullable = false)
    private int highestPrice;

    @Column(nullable = false)
    private int lowestPrice;

    @Column(nullable = false)
    private int tradingVolume;

    @Column(nullable = false)
    private Long tradingValue;

}
