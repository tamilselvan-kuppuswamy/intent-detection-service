
# 🤖 Intent Detection Service (Java + Spring Boot)

This is a Spring Boot microservice that receives text via REST API, classifies user **intent** (e.g., "CreateShipment", "TrackShipment", etc.) using **Azure OpenAI (GPT-4)**, and returns structured JSON results with confidence and explanation. Designed for logistics and shipment assistant apps.

---

## 🚀 Features

- Accepts plain text and returns intent as structured JSON
- Leverages Azure OpenAI for prompt-based intent detection
- Confidence scoring and minimum threshold enforcement
- Alias/normalization mapping for common user phrases
- Noise/filler input filtering
- Global exception handling
- Full logging and X-Correlation-ID support for distributed tracing
- Configuration-driven: Allowed intents, threshold, LLM params in YAML

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.5.3
- Azure OpenAI Service (GPT-4 or GPT-3.5)
- Lombok
- Swagger (SpringDoc)
- Maven

---

## ⚙️ Configuration

Edit `application.yaml` for your intents and Azure credentials:

```yaml
server:
  port: 5002

intent-config:
  allowed-intents:
    - CreateShipment
    - TrackShipment
    - ReturnShipment
    - RescheduleDelivery
    - ReportIssue
    - ConfirmIntent
    - CancelIntent
  confidence-threshold: 80
  temperature: 0.2
  max-tokens: 50

azure:
  openai:
    endpoint: https://<your-openai-endpoint>.openai.azure.com
    api-key: ${AZURE_OPENAI_API_KEY}
    deployment: logistics-intents
    api-version: 2024-05-01
```

**Tip:** Set your API key in the environment:  
`export AZURE_OPENAI_API_KEY=sk-...` (Linux/Mac)  
or in your IDE Run Configuration for Windows/IntelliJ.

---

## 📦 How to Build & Run

### 1. Clone and build
```sh
git clone https://github.com/your-org/intent-detection-service.git
cd intent-detection-service
mvn clean install
```

### 2. Run locally
```sh
mvn spring-boot:run
```

---

## 📨 API Usage

**Endpoint:**  
`POST /detect-intent`  
Content-Type: `application/json`  
Header: `X-Correlation-ID` (optional)

### curl:
```sh
curl -X POST http://localhost:5002/detect-intent   -H "X-Correlation-ID: demo-uuid-123"   -H "Content-Type: application/json"   -d '{"text": "I want to send a parcel to Mumbai"}'
```

### Postman:
- **Method:** POST
- **URL:** `http://localhost:5002/detect-intent`
- **Headers:**
    - Content-Type: application/json
    - X-Correlation-ID: abc-xyz-uuid (optional)
- **Body:** raw JSON
    - `{ "text": "Where is my shipment?" }`

---

## 🧾 Sample Responses

**Success:**
```json
{
  "intent": "TrackShipment",
  "confidence": 95,
  "explanation": "Detected due to keyword 'where' and 'shipment'"
}
```

**Low confidence / Not allowed intent:**
```json
{
  "intent": "Unknown",
  "confidence": 41,
  "explanation": "Low confidence or not allowed"
}
```

**Noise/filler input:**
```json
{
  "intent": "Unknown",
  "confidence": 0,
  "explanation": "Input was considered noise or filler."
}
```

**Validation error:**
```json
{
  "text": "must not be blank"
}
```

---

## 🧩 Logging & Tracing

```
INFO  [CID: abcd-efgh]: 🔍 Received detect-intent request: I want to send a package
INFO  [CID: abcd-efgh]: ✅ Intent detection completed: CreateShipment (confidence: 97)
WARN  [CID: abcd-efgh]: ⚠️ Rejected intent 'Unknown' with confidence 40
ERROR [CID: abcd-efgh]: 💥 GPT exception ...
```

---

## 🔍 Swagger UI

Swagger/OpenAPI documentation available at:  
[http://localhost:5002/swagger-ui.html](http://localhost:5002/swagger-ui.html)

---

## 📝 Best Practices

- Always provide `X-Correlation-ID` for distributed tracing
- Configure `allowed-intents` and `confidence-threshold` in YAML, not code
- Do not hardcode secrets; use env variables for credentials
- Tune temperature and max-tokens for desired model output
- See logs for full audit trail (intent, input, confidence, time)