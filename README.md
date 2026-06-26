# AmarPayBd — Payment Verification API

Spring Boot REST API for verifying bKash, Nagad, Rocket payments via Firebase Firestore.

## API Endpoints

### Verify Payment
```
POST /api/verify
Content-Type: application/json

{
  "licenseKey": "AMARPAY-XXXX-XXXX-XXXX",
  "trxId": "ABC123",
  "amount": 500,
  "method": "bKash"
}
```

### Status Check
```
GET /api/status
```

## Deploy on Render

Set environment variable:
- `GOOGLE_CREDENTIALS` = Firebase service account JSON content
