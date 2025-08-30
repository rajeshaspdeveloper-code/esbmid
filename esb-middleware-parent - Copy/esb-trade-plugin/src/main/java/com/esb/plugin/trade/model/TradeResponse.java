package com.esb.plugin.trade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Trade response model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeResponse {

    private String requestId;
    private String tradeId;
    private String orderId;
    private String status;
    private String statusMessage;
    private String customerId;
    private String accountId;
    private String instrument;
    private String side;
    private Integer quantity;
    private Integer executedQuantity;
    private BigDecimal price;
    private BigDecimal executedPrice;
    private BigDecimal totalAmount;
    private String currency;
    private String exchange;
    private LocalDateTime orderTime;
    private LocalDateTime executionTime;
    private LocalDateTime settlementDate;
    private String confirmationNumber;
    private String commissionAmount;
    private String fees;
    private Map<String, Object> tradeDetails;
    private String errorCode;
    private String errorMessage;

    public TradeResponse() {}

    public TradeResponse(String requestId, String tradeId) {
        this.requestId = requestId;
        this.tradeId = tradeId;
        this.status = "SUCCESS";
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getExecutedQuantity() {
        return executedQuantity;
    }

    public void setExecutedQuantity(Integer executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public String getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(String commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public Map<String, Object> getTradeDetails() {
        return tradeDetails;
    }

    public void setTradeDetails(Map<String, Object> tradeDetails) {
        this.tradeDetails = tradeDetails;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "TradeResponse{" +
                "requestId='" + requestId + '\'' +
                ", tradeId='" + tradeId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", customerId='" + customerId + '\'' +
                ", instrument='" + instrument + '\'' +
                ", quantity=" + quantity +
                ", executedQuantity=" + executedQuantity +
                ", price=" + price +
                '}';
    }
}