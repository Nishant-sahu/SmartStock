
package com.crio.warmup.stock.exception;

import java.util.concurrent.ExecutionException;

public class StockQuoteServiceException extends RuntimeException {


  public StockQuoteServiceException() {
    super();
  }

  public StockQuoteServiceException(String message) {
    super(message);
  }

  public StockQuoteServiceException(String message, ExecutionException e) {
    super(message);
  }

  // public StockQuoteServiceException(String message, Throwable cause) {
  // super(message, cause);
  // }
}
