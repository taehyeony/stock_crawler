package com.example.stock.crawler.service.crawler;

import com.example.stock.crawler.cache.StockInfoCache;
import com.example.stock.crawler.entity.StockInfoEntity;
import com.example.stock.crawler.entity.StockPriceEntity;
import com.example.stock.crawler.entity.enumeration.*;
import com.example.stock.crawler.repository.StockInfoRepository;
import com.example.stock.crawler.repository.StockPriceRepository;
import com.example.stock.crawler.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockCrawlerService {

    private final WebDriver driver;
    private final StockInfoRepository stockInfoRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockInfoCache stockInfoCache;

    /**
     * 주식 기본 정보 크롤링
     */
    public void getStockInfo() {
        String url = "http://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020201";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        driver.get(url);

        //css Selector
        By kospiRadioSelector = By.cssSelector("#mktId_0_1");
        By searchButtonSelector = By.cssSelector("#jsSearchButton");
        By checkTableLoadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(1) > td:nth-child(1)");
        By stockTableSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody");
        By loadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.loading-bar-wrap.small");

        //주식 정보 Set
        Set<StockInfoEntity> stockInfoList = new HashSet<>();
        int previousSize = 0;

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
            WebElement searchButtonElement = driver.findElement(searchButtonSelector);
            js.executeScript("arguments[0].click();", kospiRadioElement);
            js.executeScript("arguments[0].click();", searchButtonElement);
            Thread.sleep(50);

            //테이블에 새로운 값이 들어올 때까지 Wait
            wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(loadingSelector)));
            Thread.sleep(50);

            //테이블을 아래로 스크롤 하면서 rows를 읽어 Entity로 변환
            while(true){
                WebElement tableElement = driver.findElement(stockTableSelector);

                List<WebElement> rows = tableElement.findElements(By.cssSelector("tr"));

                //화면에 존재하는 row를 한줄 씩 읽어오기
                for(WebElement row: rows){
                    String standardCode = row.findElement(By.cssSelector("td:nth-child(1)")).getAttribute("textContent");
                    String shortCode = row.findElement(By.cssSelector("td:nth-child(2)")).getAttribute("textContent");
                    String korStockName = row.findElement(By.cssSelector("td:nth-child(3)")).getAttribute("textContent");
                    String korShortStockName = row.findElement(By.cssSelector("td:nth-child(4)")).getAttribute("textContent");
                    String engStockName = row.findElement(By.cssSelector("td:nth-child(5)")).getAttribute("textContent");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    Date listingDate = simpleDateFormat.parse(row.findElement(By.cssSelector("td:nth-child(6)")).getAttribute("textContent"));
                    MarketType marketType = MarketType.valueOf(row.findElement(By.cssSelector("td:nth-child(7)")).getAttribute("textContent"));
                    CertificateType certificateType = CertificateType.valueOf(row.findElement(By.cssSelector("td:nth-child(8)")).getAttribute("textContent"));
                    String department = row.findElement(By.cssSelector("td:nth-child(9)")).getAttribute("textContent");
                    StockType stockType = StockType.valueOf(row.findElement(By.cssSelector("td:nth-child(10)")).getAttribute("textContent"));
                    int faceValue = 0;
                    try {
                        faceValue = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(11)")).getAttribute("textContent"));
                    } catch (NumberFormatException e){
                        faceValue = -1; //무액면인 경우
                    }
                    Long listedStockNum = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(12)")).getAttribute("textContent"));
                    Thread.sleep(10);

                    StockInfoEntity stockInfo = StockInfoEntity.builder()
                            .standardCode(standardCode)
                            .shortCode(shortCode)
                            .korStockName(korStockName)
                            .korShortStockName(korShortStockName)
                            .engStockName(engStockName)
                            .listingDate(listingDate)
                            .marketType(marketType)
                            .certificateType(certificateType)
                            .department(department)
                            .stockType(stockType)
                            .faceValue(faceValue)
                            .listedStockNum(listedStockNum)
                            .build();
                    stockInfoList.add(stockInfo);
                }

                //화면을 아래로 스크롤
                js.executeScript("""
                                        let scroller = document.querySelector('#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-FREEZE-SCROLLER');
                                        scroller.scrollTop += 2200;
                                        scroller.dispatchEvent(new Event('scroll'));""");
                Thread.sleep(10);

                //새로운 주식이 set에 추가되지 않은 경우 break
                if (stockInfoList.size() == previousSize) break;
                previousSize = stockInfoList.size();
            }
            stockInfoRepository.saveAll(stockInfoList);
            System.out.println("주식 정보 크롤링 완료 : " + stockInfoList.size() + "개");
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
        // 바뀐 주식 정보를 다시 캐쉬
        stockInfoCache.loadCacheFromDatabase();
    }

    /**
     * 주식 시세 정보 크롤링
     * @param date 날짜
     */
    public void getStockPriceByDate(LocalDate date) {
        String url = "http://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        driver.get(url);

        //css Selector
        By kospiRadioSelector = By.cssSelector("#mktId_0_1");
        By dateInputSelector = By.cssSelector("#trdDd");
        By searchButtonSelector = By.cssSelector("#jsSearchButton");
        By checkTableLoadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(1) > td:nth-child(1)");
        By stockTableSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody");
        By loadingSelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.loading-bar-wrap.small");
        By isOpeningDaySelector = By.cssSelector("#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-GRID-MAIN-WRAPPER > div.CI-GRID-BODY-WRAPPER > div > div > table > tbody > tr:nth-child(1) > td:nth-child(5)");

        //주식 시세 정보 Set
        Set<StockPriceEntity> stockPriceList = new HashSet<>();
        int previousSizes = 0;

        try {
            //초기에 테이블 렌더링이 완료될 때 까지 Wait
            wait.until(driver -> {
                try {
                    return driver.findElement(checkTableLoadingSelector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            });
            //라디오 버튼 클릭, 날짜 변경 후 조회 버튼 클릭
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

            //테이블을 아래로 스크롤 하면서 rows를 읽어 Entity로 변환
            while(true) {
                WebElement tableElement = driver.findElement(stockTableSelector);

                List<WebElement> rows = tableElement.findElements(By.cssSelector("tr"));

                //화면에 존재하는 row를 한줄 씩 읽어오기
                for (WebElement row : rows) {
                    try {
                        String shortCode = row.findElement(By.cssSelector("td:nth-child(1)")).getAttribute("textContent");
                        String stockName = row.findElement(By.cssSelector("td:nth-child(2)")).getAttribute("textContent");
                        int closingPrice = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(5)")).getAttribute("textContent"));
                        int priceChange = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(6) > span")).getAttribute("textContent"));
                        BigDecimal priceChangeRate = BigDecimal.valueOf(Double.parseDouble(row.findElement(By.cssSelector("td:nth-child(7)")).getAttribute("textContent")));
                        if(priceChangeRate.signum()<0){
                            priceChange*=-1;
                        }
                        int openingPrice = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(8)")).getAttribute("textContent"));
                        int highestPrice = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(9)")).getAttribute("textContent"));
                        int lowestPrice = NumberUtils.parseCommaSeparatedInt(row.findElement(By.cssSelector("td:nth-child(10)")).getAttribute("textContent"));
                        long tradingVolume = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(11)")).getAttribute("textContent"));
                        long tradingValue = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(12)")).getAttribute("textContent"));
                        long marketCap = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(13)")).getAttribute("textContent"));
                        long listedStockNum = NumberUtils.parseCommaSeparatedLong(row.findElement(By.cssSelector("td:nth-child(14)")).getAttribute("textContent"));

                        StockInfoEntity stockInfoEntity = stockInfoCache.getStockInfo(shortCode, stockName);

                        StockPriceEntity.StockPriceEntityBuilder stockPriceBuilder = StockPriceEntity.builder()
                                .stockInfoEntity(stockInfoEntity)
                                .closingPrice(closingPrice)
                                .priceChange(priceChange)
                                .priceChangeRate(priceChangeRate)
                                .openingPrice(openingPrice)
                                .highestPrice(highestPrice)
                                .lowestPrice(lowestPrice)
                                .tradingVolume(tradingVolume)
                                .tradingValue(tradingValue)
                                .marketCap(marketCap)
                                .listedStockNum(listedStockNum)
                                .date(date);

                        Long stockPriceId = stockPriceRepository.findStockPriceIdByShortCodeAndDate(shortCode,date);
                        if(stockPriceId!=null){
                            stockPriceBuilder.stockPriceId(stockPriceId);
                        }

                        StockPriceEntity stockPrice = stockPriceBuilder.build();

                        stockPriceList.add(stockPrice);
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }

                //화면을 아래로 스크롤
                js.executeScript("""
                                        let scroller = document.querySelector('#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-FREEZE-SCROLLER');
                                        scroller.scrollTop += 2000;
                                        scroller.dispatchEvent(new Event('scroll'));""");
                Thread.sleep(10);

                //새로운 주식이 set에 추가되지 않은 경우 break
                if(stockPriceList.size()==previousSizes) break;
                previousSizes = stockPriceList.size();
            }
            stockPriceRepository.saveAll(stockPriceList);
            LocalTime now = LocalTime.now();  // 현재 시간 가져오기

            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");  // 시:분:초 포맷
            String formattedTime = now.format(formatter2);  // HH:mm:ss 문자열로 포맷

            System.out.println(formattedTime + "   주식 시세 크롤링 완료 : " + stockPriceList.size() + "개 : " + date);
        } catch (NumberFormatException e){
          e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
    }

}
