
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  static PortfolioManager port = new PortfolioManagerImpl(new RestTemplate());



  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File file = resolveFileFromResources(args[0]);
    ObjectMapper ob = getObjectMapper();

    PortfolioTrade[] trades = ob.readValue(file, PortfolioTrade[].class);

    List<String> symbolsList = new ArrayList<String>();

    for (PortfolioTrade trade : trades) {
      symbolsList.add(trade.getSymbol());
    }

    return symbolsList;
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }



  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/nsahu6398-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "29:1";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    // 0 - filename , 1 - LocalDate

    String apiToken = getToken();
    LocalDate endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> totalReturnsList = new ArrayList<TotalReturnsDto>();

    for (PortfolioTrade trade : trades) {
      List<Candle> results = fetchCandles(trade, endDate, apiToken);
      if (results != null) {
        totalReturnsList.add(
            new TotalReturnsDto(trade.getSymbol(), results.get(results.size() - 1).getClose()));
      }
    }

    Collections.sort(totalReturnsList, new Comparator<TotalReturnsDto>() {
      @Override
      public int compare(TotalReturnsDto t1, TotalReturnsDto t2) {
        return (int) t1.getClosingPrice().compareTo(t2.getClosingPrice());
      }

    });


    List<String> finalSymbols = new ArrayList<String>();

    for (TotalReturnsDto t : totalReturnsList) {
      finalSymbols.add(t.getSymbol());
    }

    return finalSymbols;
  }


  public static String getToken() {
    // return "015a2a9ebfa7dce4c7357a52010d1c2d1b68a3d0";
    return "dd0221edb706e9fe702e6fb7d8e00523259f34b4";
  }


  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper ob = getObjectMapper();

    List<PortfolioTrade> trades = ob.readValue(file, new TypeReference<List<PortfolioTrade>>() {});
    return trades;
  }



  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {

    // check if pruchase date is more than end date

    // if (endDate.isBefore(trade.getPurchaseDate())) {
    // throw new RuntimeException("End Date is less than Purchase Date");
    // }

    String baseUrl = "https://api.tiingo.com/tiingo/daily/";
    String url = baseUrl + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate()
        + "&endDate=" + endDate + "&token=" + token;

    return url;
  }


  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate rTemplate = new RestTemplate();
    String url = prepareUrl(trade, endDate, token);
    TiingoCandle[] candles = rTemplate.getForObject(url, TiingoCandle[].class);
    List<Candle> res = Arrays.asList(candles);
    return res;
  }


  public static PortfolioTrade[] fetchPortfolioTrade(String fileName)
      throws URISyntaxException, StreamReadException, DatabindException, IOException {

    File file = resolveFileFromResources(fileName);
    ObjectMapper ob = getObjectMapper();

    PortfolioTrade[] trades = ob.readValue(file, PortfolioTrade[].class);

    return trades;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    PortfolioTrade[] trades = fetchPortfolioTrade(args[0]);
    String token = getToken();
    LocalDate endDate = LocalDate.parse(args[1]);
    List<AnnualizedReturn> annualReturns = new ArrayList<AnnualizedReturn>();

    for (PortfolioTrade trade : trades) {

      List<Candle> candles = fetchCandles(trade, endDate, token);
      AnnualizedReturn ret = calculateAnnualizedReturns(endDate, trade,
          getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));

      if (ret != null) {
        annualReturns.add(ret);
      }

    }

    List<AnnualizedReturn> fAnnualizedReturns = annualReturns.stream()
        .sorted((r1, r2) -> r1.getAnnualizedReturn().compareTo(r2.getAnnualizedReturn()))
        .collect(Collectors.toList());;

    Collections.reverse(fAnnualizedReturns);

    return fAnnualizedReturns;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    double totalReturn = (sellPrice - buyPrice) / buyPrice;

    // int total_num_years = Period.between(trade.getPurchaseDate(), endDate).getYears();
    Double total_num_years =
        (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365;

    double annualized_returns = Math.pow(1 + totalReturn, (1 / total_num_years)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }



  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Once you are done with the implementation inside PortfolioManagerImpl and
  // PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  // Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  // call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  // public static String readFileAsString(String fileName, ObjectMapper ob)
  // throws StreamReadException, DatabindException, IOException, URISyntaxException {
  // File file = resolveFileFromResources(fileName);
  // Portfolio trades = ob.readValue(file, String.class);
  // return trades;

  // }
  public static String readFileAsString(String file) throws Exception {
    return new String(Files.readAllBytes(Paths.get(file)));
  }


  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    ObjectMapper objectMapper = getObjectMapper();

    // return new ArrayList<AnnualizedReturn>();
    String contents = readFileAsString(file);
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);


    return port.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

// ./gradlew verifyMavenJarsCreated
