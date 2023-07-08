
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  protected StockQuotesService stockQuotesService;
  protected RestTemplate restTemplate;
  // RestTemplate restTemplate=new RestTemplate();

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  // @Deprecated
  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
  // this.stockQuotesService = stockQuotesService;
  // }

  protected PortfolioManagerImpl(StockQuotesService stockQuoteService) {
    // RestTemplate restTemplate = new RestTemplate();
    // this.restTemplate = restTemplate;
    this.stockQuotesService = stockQuoteService;
  }


  // : CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF


  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws StockQuoteServiceException {

    List<AnnualizedReturn> annualReturns = new ArrayList<AnnualizedReturn>();
    // String token = getApiKey();

    List<Candle> candles = new ArrayList<>();

    for (PortfolioTrade trade : portfolioTrades) {


      try {
        candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);

        AnnualizedReturn ret = calculateAnnualReturn(endDate, trade,
            getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));

        annualReturns.add(ret);

      } catch (JsonProcessingException e) {

        throw new StockQuoteServiceException(e.getMessage());
      }

      // if (ret != null) {

      // }

    }

    annualReturns = annualReturns.stream().sorted(getComparator()).collect(Collectors.toList());

    return annualReturns;
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }


  private AnnualizedReturn calculateAnnualReturn(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;
    Double total_num_years =
        (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365;

    Double annualized_returns = Math.pow(1 + totalReturn, (1 / total_num_years)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }


  // private List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
  // String url = buildUri(trade.getSymbol(), trade.getPurchaseDate(), endDate);
  // TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
  // return Arrays.asList(candles);
  // //return res;
  // }



  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    // String url = buildUri(symbol, from, to);
    // TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
    // List<Candle> res = Arrays.asList(candles);

    return stockQuotesService.getStockQuote(symbol, from, to);
    // return res;
  }


  private String getApiKey() {

    List<String> keys = new ArrayList<String>();
    keys.add("015a2a9ebfa7dce4c7357a52010d1c2d1b68a3d0");
    keys.add("65ebd0ae0073fee44387941457a798b175abdc2b");
    keys.add("dd0221edb706e9fe702e6fb7d8e00523259f34b4");
    return keys.get(0);

  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String token = getApiKey();

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate + "&endDate=" + endDate + "&token=" + token;

    return uriTemplate;
  }


  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate)
      throws StockQuoteServiceException {
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol();


    Double buyPrice = 0.0, sellPrice = 0.0;


    try {
      LocalDate startLocalDate = trade.getPurchaseDate();
      List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);


      Collections.sort(stocksStartToEndFull, (candle1, candle2) -> {
        return candle1.getDate().compareTo(candle2.getDate());
      });

      Candle stockStartDate = stocksStartToEndFull.get(0);
      Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);


      buyPrice = stockStartDate.getOpen();
      sellPrice = stocksLatest.getClose();
      endDate = stocksLatest.getDate();


    } catch (JsonProcessingException e) {
      throw new RuntimeException();
    }
    Double totalReturn = (sellPrice - buyPrice) / buyPrice;


    long daysBetweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
    Double totalYears = (double) (daysBetweenPurchaseAndSelling) / 365;


    Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;
    return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);


  }



  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {

    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
    final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    
    for (int i = 0; i < portfolioTrades.size(); i++) {
      PortfolioTrade trade = portfolioTrades.get(i);
      Callable<AnnualizedReturn> callableTask = () -> {
        return getAnnualizedReturn(trade, endDate);
      };
      Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
      futureReturnsList.add(futureReturns);
    }

    for (int i = 0; i < portfolioTrades.size(); i++) {
      Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
      try {
        AnnualizedReturn returns = futureReturns.get();
        annualizedReturns.add(returns);
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Error when calling the API", e);

      }
    }
    // Collections.sort(annualizedReturns, Collections.reverseOrder());
    return annualizedReturns.stream().sorted(getComparator()).collect(Collectors.toList());
  }


}
