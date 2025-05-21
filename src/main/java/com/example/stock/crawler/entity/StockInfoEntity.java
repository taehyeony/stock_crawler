package com.example.stock.crawler.entity;

import com.example.stock.crawler.entity.enumeration.CertificateType;
import com.example.stock.crawler.entity.enumeration.MarketType;
import com.example.stock.crawler.entity.enumeration.StockType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name="stock_info")
@Table(name="stock_info")
public class StockInfoEntity {

    @Id
    @Column(length = 15,nullable = false) //길이 15, 빈칸 비 허용
    private String shortCode;

    @Column(length = 30,nullable = false) //길이 30, 빈칸 비 허용
    private String standardCode;

    @Column(length = 50,nullable = false) //길이 50, 빈칸 비 허용
    private String korStockName;

    @Column(length = 20,nullable = false) //길이 20, 빈칸 비 허용
    private String korShortStockName;

    @Column(length = 100,nullable = false) //길이 100, 빈칸 비 허용
    private String engStockName;

    @Column(nullable = false) //빈칸 비 허용
    private LocalDate listingDate;

    @Enumerated(EnumType.STRING) // enum 타입 사용 선언
    @Column(nullable = false)
    private MarketType marketType;

    @Enumerated(EnumType.STRING) // enum 타입 사용 선언
    @Column(nullable = false)
    private CertificateType certificateType;

    @Column(length = 100,nullable = false) //길이 100, 빈칸 비 허용
    private String department;

    @Enumerated(EnumType.STRING) // enum 타입 사용 선언
    @Column(nullable = false)
    private StockType stockType;

    @Column(nullable = false) //빈칸 비 허용
    private int faceValue;

    @Column(nullable = false) //길이 20, 빈칸 비 허용
    private Long listedStockNum;

    @OneToMany(mappedBy = "stockInfoEntity", fetch = FetchType.LAZY) //바로 로딩이 필요하면 EAGER로 변경
    private List<StockPriceEntity> stockPriceEntities;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockInfoEntity stockInfoEntity = (StockInfoEntity) o;
        return Objects.equals(shortCode, stockInfoEntity.shortCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortCode);
    }
}
