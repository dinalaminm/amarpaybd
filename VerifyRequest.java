package com.amarpaybd.model;

public class VerifyRequest {
    private String licenseKey;
    private String trxId;
    private Double amount;   // optional — match করতে চাইলে
    private String method;   // optional — "bKash", "Nagad", "Rocket"

    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }

    public String getTrxId() { return trxId; }
    public void setTrxId(String trxId) { this.trxId = trxId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
