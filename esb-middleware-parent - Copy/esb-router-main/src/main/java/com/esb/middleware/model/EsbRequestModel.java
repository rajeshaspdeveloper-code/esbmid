package com.esb.middleware.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsbRequestModel {
    
    @Valid
    @NotNull
    private EsbHeader esbHeader;
    
    @Valid
    @NotNull
    private EsbBody esbBody;
    
    // Getters and setters
    public EsbHeader getEsbHeader() { return esbHeader; }
    public void setEsbHeader(EsbHeader esbHeader) { this.esbHeader = esbHeader; }
    
    public EsbBody getEsbBody() { return esbBody; }
    public void setEsbBody(EsbBody esbBody) { this.esbBody = esbBody; }
    
    public static class EsbHeader {
        private String country;
        private String targetApplication;
        private String requestTimeStamp;
        private String serviceId;
        private String uuid;
        private String branch;
        private String sourceApplication;
        
        // Getters and setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getTargetApplication() { return targetApplication; }
        public void setTargetApplication(String targetApplication) { this.targetApplication = targetApplication; }
        
        public String getRequestTimeStamp() { return requestTimeStamp; }
        public void setRequestTimeStamp(String requestTimeStamp) { this.requestTimeStamp = requestTimeStamp; }
        
        public String getServiceId() { return serviceId; }
        public void setServiceId(String serviceId) { this.serviceId = serviceId; }
        
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        
        public String getSourceApplication() { return sourceApplication; }
        public void setSourceApplication(String sourceApplication) { this.sourceApplication = sourceApplication; }
    }
    
    public static class EsbBody {
        private String correlationId;
        private String company;
        private String userName;
        private Transaction transaction;
        
        // Getters and setters
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public Transaction getTransaction() { return transaction; }
        public void setTransaction(Transaction transaction) { this.transaction = transaction; }
        
        public static class Transaction {
            private String application;
            private String operation;
            private String version;
            private String transactionId;
            
            // Getters and setters
            public String getApplication() { return application; }
            public void setApplication(String application) { this.application = application; }
            
            public String getOperation() { return operation; }
            public void setOperation(String operation) { this.operation = operation; }
            
            public String getVersion() { return version; }
            public void setVersion(String version) { this.version = version; }
            
            public String getTransactionId() { return transactionId; }
            public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        }
    }
}