package com.esb.plugin.trade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Trade request model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeRequest {

    private String requestId;
    private String branchCode;
    private String correlationId;
    private String sourceSystem;
    private String tradeId;
    private String customerId;
    private String accountId;
    private String instrument;
    private String instrumentType;
    private String side; // BUY, SELL
    private Integer quantity;
    private BigDecimal price;
    private String orderType; // MARKET, LIMIT
    private String timeInForce; // DAY, GTC, IOC
    private LocalDateTime orderTime;
    private String currency;
    private String exchange;
    private String confirmationAction; // CONFIRM, REJECT, MODIFY
    private Map<String, Object> additionalData;

    public TradeRequest() {}

    public TradeRequest(String tradeId, String customerId) {
        this.tradeId = tradeId;
        this.customerId = customerId;
        this.orderTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
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

    public String getConfirmationAction() {
        return confirmationAction;
    }

    public void setConfirmationAction(String confirmationAction) {
        this.confirmationAction = confirmationAction;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "TradeRequest{" +
                "requestId='" + requestId + '\'' +
                ", tradeId='" + tradeId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", instrument='" + instrument + '\'' +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderType='" + orderType + '\'' +
                '}';
    }
}