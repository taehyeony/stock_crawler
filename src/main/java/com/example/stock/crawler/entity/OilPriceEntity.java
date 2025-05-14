package com.example.stock.crawler.entity;

import com.example.stock.crawler.entity.enumeration.OilType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name="oil_price")
@Table(
        name="oil_price",
        uniqueConstraints = @UniqueConstraint(columnNames = {"oil_type","date"})
)
public class OilPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int oilId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OilType oilType;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal averagePriceCompetition;

    @Column(nullable = false,precision = 7,scale = 2)
    private BigDecimal averagePriceConsultation;

    @Column(nullable = false)
    private Long tradingVolume;

    @Column(nullable = false)
    private Long tradingValue;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private LocalDate date;
}
