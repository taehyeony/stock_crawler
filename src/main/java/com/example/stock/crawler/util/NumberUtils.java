package com.example.stock.crawler.util;

import java.math.BigDecimal;

public class NumberUtils {
    /**
     * ,가 포함된 숫자를 int형으로 변환하는 함수
     * @param numberWithCommas
     * @return int 타입 숫자
     * @throws NumberFormatException
     */
    public static int parseCommaSeparatedInt(String numberWithCommas) throws NumberFormatException {
        // 쉼표 제거 후 int로 변환
        return Integer.parseInt(numberWithCommas.replace(",", ""));
    }

    /**
     * ,가 포함된 숫자를 Long형으로 변환하는 함수
     * @param numberWithCommas ,가 포함된 숫자 문자열
     * @return Long 타입 숫자
     * @throws NumberFormatException
     */
    public static Long parseCommaSeparatedLong(String numberWithCommas) throws NumberFormatException {
        // 쉼표 제거 후 int로 변환
        return Long.parseLong(numberWithCommas.replace(",", ""));
    }

    /**
     * ,가 포함된 숫자를 BigDecimal로 변환하는 함수
     * @param numberWithCommas ,가 포함된 숫자 문자열
     * @return BigDecimal 타입 숫자
     * @throws NumberFormatException
     */
    public static BigDecimal parseCommaSeparatedBigDecimal(String numberWithCommas) throws NumberFormatException {
        // 쉼표 제거 후 BigDecimal로 변환
        return new BigDecimal(numberWithCommas.replace(",", ""));
    }
}
