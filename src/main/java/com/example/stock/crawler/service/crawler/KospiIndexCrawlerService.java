package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.entity.KospiIndexEntity;
import com.example.stock.crawler.repository.KospiIndexRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class KospiIndexCrawlerService {

    private final WebDriver driver;
    private final KospiIndexRepository kospiIndexRepository;

    /**
     * 코스피 지수 크롤링
     * @param date 날짜
     */
    public void getKospiByDate(LocalDate date) {
        String url = "http://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201010101";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        driver.get(url);

        //css Selector
        By kospiRadioSelector = By.cssSelector("#idxIndMidclssCd_0_1");
        By dateInputSelector = By.cssSelector("#trdDd");
        By searchButtonSelector = By.cssSelector("#jsSearchButton");
        By checkTableLoadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(1) > td:nth-child(1)");
        By stockTableSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody");
        By loadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.loading-bar-wrap.small");
        By isOpeningDaySelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(2) > td:nth-child(2)");

        try {
            //초기에 테이블 렌더링이 완료될 때 까지 Wait
            wait.until(driver -> {
                try {
                    return driver.findElement(checkTableLoadingSelector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            });

            //라디오 버튼 클릭 후 조회 버튼 클릭
            WebElement kospiRadioElement = driver.findElement(kospiRadioSelector);
            WebElement dateInputElement = driver.findElement(dateInputSelector);
            WebElement searchButtonElement = driver.findElement(searchButtonSelector);
            js.executeScript("arguments[0].click();", kospiRadioElement);
            js.executeScript("arguments[0].value = arguments[1];", dateInputElement, formatter.format(date));
            js.executeScript("arguments[0].click();", searchButtonElement);
            Thread.sleep(50);

            //테이블에 새로운 값이 들어올 때까지 Wait
            wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(loadingSelector)));
            Thread.sleep(50);

            // 휴장일 인지 확인
            WebElement tableElementTemp = driver.findElement(stockTableSelector);

            boolean isClosingDay = tableElementTemp.findElement(isOpeningDaySelector).getText().equals("-");
            if(isClosingDay){
                return;
            }

            //테이블을 읽어 코스피 지수의 정보만 읽어 Entity로 변환
            WebElement tableElement = driver.findElement(stockTableSelector);

            List<WebElement> rows = tableElement.findElements(By.cssSelector("tr"));

            KospiIndexEntity kospiIndexEntity = null;
            for(WebElement row: rows){
                boolean isKospi = row.findElement(By.cssSelector("td:nth-child(1)")).getAttribute("textContent").equals("코스피");
                if(isKospi){
                    BigDecimal closingPrice = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(2)")).getAttribute("textContent"));
                    BigDecimal kospiChange = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(3) > span")).getAttribute("textContent"));
                    BigDecimal kospiChangeRate = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(4) > span")).getAttribute("textContent"));
                    if(kospiChangeRate.signum()<0){
                        kospiChange = kospiChange.negate();
                    }
                    BigDecimal openingPrice = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(5)")).getAttribute("textContent"));
                    BigDecimal highestPrice = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(6)")).getAttribute("textContent"));
                    BigDecimal lowestPrice = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(7)")).getAttribute("textContent"));
                    int tradingVolume = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(8)")).getAttribute("textContent"));
                    Long tradingValue = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(9)")).getAttribute("textContent"));
                    Long marketCap = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(10)")).getAttribute("textContent"));

                    kospiIndexEntity = KospiIndexEntity.builder()
                            .date(date)
                            .closingPrice(closingPrice)
                            .kospiChange(kospiChange)
                            .kospiChangeRate(kospiChangeRate)
                            .openingPrice(openingPrice)
                            .highestPrice(highestPrice)
                            .lowestPrice(lowestPrice)
                            .tradingVolume(tradingVolume)
                            .tradingValue(tradingValue)
                            .marketCap(marketCap)
                            .build();

                    kospiIndexRepository.save(kospiIndexEntity);
                    break;
                }
            }
            LocalTime now = LocalTime.now();  // 현재 시간 가져오기

            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");  // 시:분:초 포맷
            String formattedTime = now.format(formatter2);  // HH:mm:ss 문자열로 포맷

            System.out.println(formattedTime + "   코스피 지수 크롤링 완료 : " + date);
        } catch (DataIntegrityViolationException e){
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
    }
}
