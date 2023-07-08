
package com.crio.warmup.stock.quotes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

        List<Candle> results= new ArrayList<>();
    try{

      String apiToken = getApiKey();
    
    String url = prepareUrl(symbol, from ,to, apiToken);
    // this.restTemplate = new RestTemplate();

    ObjectMapper mapper= new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    String response =  restTemplate.getForObject(url, String.class);

    // TiingoCandle[] candles =  restTemplate.getForObject(response, TiingoCandle[].class);
    TiingoCandle[] candles=mapper.readValue(response, TiingoCandle[].class);
    
     results= Arrays.asList(candles);
    }catch(Exception e){
      throw new StockQuoteServiceException(e.getMessage());
    }    
    
  
    return results;
  }
 

  public static String prepareUrl(String symbol, LocalDate startDate, LocalDate endDate, String token){

     // check if pruchase date is more than end date
    //  if (endDate.isBefore(startDate)) {
    //   throw new RuntimeException("End Date is less than Purchase Date");
    // }

    String baseUrl = "https://api.tiingo.com/tiingo/daily/";
    String url = baseUrl + symbol + "/prices?startDate=" + startDate
    + "&endDate=" + endDate + "&token=" + token;

    return url;
  }

  private String getApiKey() {

    List<String> keys = new ArrayList<String>();
    keys.add("015a2a9ebfa7dce4c7357a52010d1c2d1b68a3d0");
    keys.add("dd0221edb706e9fe702e6fb7d8e00523259f34b4");

    return keys.get(0);

  }



  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest






}
