package com.example.stock.crawler.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoldType {
    GOLD_1KG("금 1kg"),
    MINI_GOLD_100G("미니금 100g");

    private final String label;
}
