package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.entity.ExchangeRateEntity;
import com.example.stock.crawler.entity.GoldPriceEntity;
import com.example.stock.crawler.entity.enumeration.GoldType;
import com.example.stock.crawler.repository.ExchangeRateRepository;
import com.example.stock.crawler.repository.GoldPriceRepository;
import com.example.stock.crawler.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateCrawlerService {

    private final WebDriver driver;
    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * 환율 크롤링
     * @param date 날짜
     */
    public void getExchangeRateByDate(LocalDate date) {
        String url = "http://www.smbs.biz/ExRate/StdExRate.jsp";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        driver.get(url);

        //css Selector
        By checkTableLoadingSelector = By.cssSelector("#frm_SearchDate > div:nth-child(17) > table > tbody");
        By loadingSelector = By.cssSelector("#frm_SearchDate > div:nth-child(17) > table > tbody > tr.brb0 > td");
        By stockTableSelector = By.cssSelector("#frm_SearchDate > div:nth-child(17) > table > tbody > tr.brb0");
        By isOpeningDaySelector = By.cssSelector("td > p");

        try {
            //초기에 테이블 렌더링이 완료될 때 까지 Wait
            wait.until(driver -> {
                try {
                    return driver.findElement(checkTableLoadingSelector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            });

            // 날짜 구성
            String year = String.valueOf(date.getYear());
            String month = String.format("%02d", date.getMonthValue());
            String day = String.format("%02d", date.getDayOfMonth());

            // 시작일과 종료일 모두 동일한 날짜로 설정
            js.executeScript("document.getElementsByName('StrSch_sYear')[0].value='" + year + "';");
            js.executeScript("document.getElementsByName('StrSch_sMonth')[0].value='" + month + "';");
            js.executeScript("document.getElementsByName('StrSch_sDay')[0].value='" + day + "';");
            js.executeScript("document.getElementsByName('StrSch_eYear')[0].value='" + year + "';");
            js.executeScript("document.getElementsByName('StrSch_eMonth')[0].value='" + month + "';");
            js.executeScript("document.getElementsByName('StrSch_eDay')[0].value='" + day + "';");

            // 폼 제출 (onsubmit="return false;"이므로 강제 submit)
            js.executeScript("document.getElementById('frm_SearchDate').submit();");
            Thread.sleep(50);

            // 휴장일 인지 확인
            WebElement tableElementTemp = wait.until(ExpectedConditions.visibilityOfElementLocated(stockTableSelector));

            boolean isClosingDay;
            try{
               isClosingDay  = tableElementTemp.findElement(isOpeningDaySelector).getText().equals("등록된 자료가 없습니다.");
            }catch (NoSuchElementException e){
                isClosingDay = false;
            }

            if(isClosingDay){
                return;
            }

            //테이블을 읽어 환율 정보를 Entity로 변환
            boolean isRightDate = tableElementTemp.findElement(By.cssSelector("td:nth-child(1)")).getText().equals(year+"."+month+"."+day);
            if(!isRightDate){
                System.out.println(tableElementTemp.findElement(By.cssSelector("td:nth-child(1)")).getText());
                System.out.println("날짜 not matched");
                return;
            }
            String currencyCode = tableElementTemp.findElement(By.cssSelector("td:nth-child(2)")).getText();
            BigDecimal exchangeRate = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(3)")).getText());
            BigDecimal change = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(4) > span")).getText());
            if(!(change.compareTo(BigDecimal.ZERO) == 0)){
                if(tableElementTemp.findElement(By.cssSelector("td:nth-child(4) > span > span")).getAttribute("class").equals("ico ico_down")){
                    change = change.negate();
                }
            }
            BigDecimal openingPrice = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(5)")).getText());
            BigDecimal highestPrice = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(6)")).getText());
            BigDecimal lowestPrice = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(7)")).getText());
            BigDecimal closingPrice = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(8)")).getText());
            BigDecimal tradingVolume = NumberUtils.parseCommaSeparatedBigDecimal(tableElementTemp.findElement(By.cssSelector("td:nth-child(9)")).getText());

            Optional<ExchangeRateEntity> optionalEntity =
                    exchangeRateRepository.findByDateAndCurrencyCode(date, currencyCode);

            ExchangeRateEntity.ExchangeRateEntityBuilder builder = ExchangeRateEntity.builder()
                    .currencyCode(currencyCode)
                    .date(date)
                    .exchangeRate(exchangeRate)
                    .change(change)
                    .openingPrice(openingPrice)
                    .highestPrice(highestPrice)
                    .lowestPrice(lowestPrice)
                    .closingPrice(closingPrice)
                    .tradingVolume(tradingVolume);

            optionalEntity.ifPresent(existingEntity -> builder.exchangeId(existingEntity.getExchangeId()));

            ExchangeRateEntity exchangeRateEntity = builder.build();
            exchangeRateRepository.save(exchangeRateEntity);

            LocalTime now = LocalTime.now();  // 현재 시간 가져오기

            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");  // 시:분:초 포맷
            String formattedTime = now.format(formatter2);  // HH:mm:ss 문자열로 포맷

            System.out.println(formattedTime + "   환율 크롤링 완료 : " + date);
        } catch (DataIntegrityViolationException e){
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
    }
}
