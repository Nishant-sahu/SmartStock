
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  public static PortfolioManager getPortfolioManager(String provider , RestTemplate restTemplate) {
    // PortfolioManagerImpl portfolioManager = new PortfolioManagerImpl(restTemplate);
    // return portfolioManager;
    return new PortfolioManagerImpl(StockQuoteServiceFactory.INSTANCE.getService(provider, restTemplate));
  }

  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    PortfolioManagerImpl portfolioManager = new PortfolioManagerImpl(restTemplate);
    return portfolioManager;
  }


}
