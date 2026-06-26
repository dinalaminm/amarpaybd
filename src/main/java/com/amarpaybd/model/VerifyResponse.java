package com.amarpaybd.model;

public class VerifyResponse {
    private boolean verified;
    private String reason;
    private String trxId;
    private Double amount;
    private String method;
    private String sender;
    private String timestamp;

    public static VerifyResponse success(String trxId, Double amount, String method, String sender, String timestamp) {
        VerifyResponse r = new VerifyResponse();
        r.verified  = true;
        r.trxId     = trxId;
        r.amount    = amount;
        r.method    = method;
        r.sender    = sender;
        r.timestamp = timestamp;
        return r;
    }

    public static VerifyResponse fail(String reason) {
        VerifyResponse r = new VerifyResponse();
        r.verified = false;
        r.reason   = reason;
        return r;
    }

    public boolean isVerified()  { return verified; }
    public String getReason()    { return reason; }
    public String getTrxId()     { return trxId; }
    public Double getAmount()    { return amount; }
    public String getMethod()    { return method; }
    public String getSender()    { return sender; }
    public String getTimestamp() { return timestamp; }
}
