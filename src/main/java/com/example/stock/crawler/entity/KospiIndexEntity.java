package com.example.stock.crawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name="kospi_index")
@Table(name="kospi_index")
public class KospiIndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int kospiId;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date date;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal closingPrice;

    @Column(nullable = false,precision = 5,scale = 2)
    private BigDecimal kospiChange;

    @Column(nullable = false,precision = 5,scale = 2)
    private BigDecimal kospiChangeRate;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal openingPrice;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal highPrice;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal lowPrice;

    @Column(nullable = false)
    private Long tradingVolume;

    @Column(nullable = false)
    private Long tradingValue;

    @Column(nullable = false)
    private Long marketCap;
}
