package com.example.stock.crawler.crawler;

import com.example.stock.crawler.entity.StockInfoEntity;
import com.example.stock.crawler.entity.enumeration.*;
import com.example.stock.crawler.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockCrawlerService {

    private final WebDriver driver;
    private final StockInfoRepository stockInfoRepository;

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

        Set<StockInfoEntity> stockInfoList = new HashSet<>();
        int previousSize = 0;

        try {
            WebElement tableTemp = wait.until(driver -> {
                try {
                    return driver.findElement(checkTableLoadingSelector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            });

            WebElement kospiRadioElement = driver.findElement(kospiRadioSelector);
            WebElement searchButtonElement = driver.findElement(searchButtonSelector);
            js.executeScript("arguments[0].click();", kospiRadioElement);
            js.executeScript("arguments[0].click();", searchButtonElement);
            Thread.sleep(100);


            wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(loadingSelector)));
            Thread.sleep(100);


            while(true){
                WebElement tableElement = driver.findElement(stockTableSelector);

                List<WebElement> rows = tableElement.findElements(By.cssSelector("tr"));

                for(WebElement row: rows){
                    String standardCode = row.findElement(By.cssSelector("td:nth-child(1)")).getText();
                    if(standardCode.isBlank()){
                        continue;
                    }
                    String shortCode = row.findElement(By.cssSelector("td:nth-child(2)")).getText();
                    String korStockName = row.findElement(By.cssSelector("td:nth-child(3)")).getText();
                    String korShortStockName = row.findElement(By.cssSelector("td:nth-child(4)")).getText();
                    String engStockName = row.findElement(By.cssSelector("td:nth-child(5)")).getText();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    Date listingDate = simpleDateFormat.parse(row.findElement(By.cssSelector("td:nth-child(6)")).getText());
                    MarketType marketType = MarketType.valueOf(row.findElement(By.cssSelector("td:nth-child(7)")).getText());

                    js.executeScript("""
                                        let scroller = document.querySelector('#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-FREEZE-SCROLLER');
                                        scroller.scrollLeft += 700;
                                        scroller.dispatchEvent(new Event('scroll'));""");
                    Thread.sleep(50);

                    CertificateType certificateType = CertificateType.valueOf(row.findElement(By.cssSelector("td:nth-child(8)")).getText());
                    String department = row.findElement(By.cssSelector("td:nth-child(9)")).getText();
                    StockType stockType = StockType.valueOf(row.findElement(By.cssSelector("td:nth-child(10)")).getText());
                    int faceValue = 0;
                    try {
                        faceValue = Integer.parseInt(row.findElement(By.cssSelector("td:nth-child(11)")).getText().replace(",",""));
                    } catch (NumberFormatException e){
                        faceValue = -1; //무액면인 경우
                    }
                    Long listedStockNum = Long.valueOf(row.findElement(By.cssSelector("td:nth-child(12)")).getText().replace(",",""));

                    js.executeScript("""
                                        let scroller = document.querySelector('#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-FREEZE-SCROLLER');
                                        scroller.scrollLeft -= 700;
                                        scroller.dispatchEvent(new Event('scroll'));""");
                    Thread.sleep(100);

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
                    System.out.println(stockInfo.toString());
                    stockInfoList.add(stockInfo);
                }

                js.executeScript("""
                                        let scroller = document.querySelector('#jsMdiContent > div > div.CI-GRID-AREA.CI-GRID-ON-WINDOWS > div.CI-GRID-WRAPPER > div.CI-FREEZE-SCROLLER');
                                        scroller.scrollTop += 500;
                                        scroller.dispatchEvent(new Event('scroll'));""");
                Thread.sleep(50);

                if (stockInfoList.size() == previousSize) break;
                previousSize = stockInfoList.size();
            }
            System.out.println(stockInfoList.size());
            stockInfoRepository.saveAll(stockInfoList);
        } catch (TimeoutException e) {
            System.out.println("타임아웃: 요소를 찾지 못했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생");
        }
    }

}
