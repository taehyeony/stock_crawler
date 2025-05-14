package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.entity.KospiIndexEntity;
import com.example.stock.crawler.entity.OilPriceEntity;
import com.example.stock.crawler.entity.enumeration.OilType;
import com.example.stock.crawler.repository.KospiIndexRepository;
import com.example.stock.crawler.repository.OilPriceRepository;
import com.example.stock.crawler.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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

@Service
@RequiredArgsConstructor
public class OilCrawlerService {

    private final WebDriver driver;
    private final OilPriceRepository oilPriceRepository;

    /**
     * 유가 크롤링
     * @param date 날짜
     */
    public void getKospiByDate(LocalDate date) {
        String url = "http://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201060102";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        driver.get(url);

        //css Selector
        By oilTypeSelector = By.cssSelector("#secugrpId");
        By startDateInputSelector = By.cssSelector("#strtDd");
        By endDateInputSelector = By.cssSelector("#endDd");
        By searchButtonSelector = By.cssSelector("#jsSearchButton");
        By checkTableLoadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(1) > td:nth-child(1)");
        By stockTableSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody");
        By loadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.loading-bar-wrap.small");
        By isOpeningDaySelector = By.cssSelector("tr:nth-child(1) > td:nth-child(1)");

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

            WebElement startDateInputElement = driver.findElement(startDateInputSelector);
            WebElement endDateInputElement = driver.findElement(endDateInputSelector);
            WebElement searchButtonElement = driver.findElement(searchButtonSelector);
            js.executeScript("arguments[0].value = arguments[1];", startDateInputElement, formatter.format(date));
            js.executeScript("arguments[0].value = arguments[1];", endDateInputElement, formatter.format(date));
            js.executeScript("arguments[0].click();", searchButtonElement);
            Thread.sleep(50);

            //테이블에 새로운 값이 들어올 때까지 Wait
            wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(loadingSelector)));
            Thread.sleep(50);

            // 휴장일 인지 확인
            WebElement tableElementTemp = driver.findElement(stockTableSelector);

            boolean isClosingDay = tableElementTemp.findElement(isOpeningDaySelector).getText().equals("데이터가 없습니다.");
            if(isClosingDay){
                return;
            }

            //테이블을 읽어 유가 정보를 읽어 Entity로 변환
            List<OilPriceEntity> oilPriceList = new ArrayList<>();
            List<String> oilTypeList = List.of("휘발유", "경유", "등유");

            for(String oilType: oilTypeList){
                WebElement oilTypeElement = driver.findElement(oilTypeSelector);
                Select select = new Select(oilTypeElement);
                select.selectByVisibleText(oilType);
                js.executeScript("arguments[0].click();", searchButtonElement);

                //테이블에 새로운 값이 들어올 때까지 Wait
                wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(loadingSelector)));
                Thread.sleep(50);

                WebElement tableElement = driver.findElement(stockTableSelector);

                WebElement row = tableElement.findElement(By.cssSelector("tr"));

                BigDecimal averagePriceCompetition = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(2)")).getAttribute("textContent"));
                BigDecimal averagePriceConsultation = NumberUtils.parseCommaSeparatedBigDecimal(row.findElement(By.cssSelector("td:nth-child(3)")).getAttribute("textContent"));
                Long tradingVolume = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(4)")).getAttribute("textContent"));
                Long tradingValue = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(5)")).getAttribute("textContent"));


                OilPriceEntity oilPriceEntity = OilPriceEntity.builder()
                        .date(date)
                        .oilType(OilType.valueOf(oilType))
                        .averagePriceCompetition(averagePriceCompetition)
                        .averagePriceConsultation(averagePriceConsultation)
                        .tradingVolume(tradingVolume)
                        .tradingValue(tradingValue)
                        .build();

                oilPriceList.add(oilPriceEntity);
            }
            oilPriceRepository.saveAll(oilPriceList);

            LocalTime now = LocalTime.now();  // 현재 시간 가져오기

            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");  // 시:분:초 포맷
            String formattedTime = now.format(formatter2);  // HH:mm:ss 문자열로 포맷

            System.out.println(formattedTime + "   유가 크롤링 완료 : " + date);
        } catch (DataIntegrityViolationException e){
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
    }
}
