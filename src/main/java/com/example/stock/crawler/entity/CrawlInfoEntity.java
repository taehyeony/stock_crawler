package com.example.stock.crawler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="crawl_info")
@Table(name="crawl_info")
public class CrawlInfoEntity {
    @Id
    private Integer id = 1;  // 고정값 1로 사용

    @Column(nullable = false)
    private LocalDate lastCrawledDate;

    @Column(nullable = false, insertable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void setLastCrawledDate(LocalDate lastCrawledDate) {
        this.lastCrawledDate = lastCrawledDate;
    }
}
