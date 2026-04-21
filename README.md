# VANET Backend — Phase 1

Quantum-Safe Blockchain VANET Road Safety System  
Spring Boot 3 + PostgreSQL + YOLOv8 (Flask)

---

## Project Structure

```
com.vanet/
├── VanetApplication.java          ← Entry point
├── controller/
│   └── RoadController.java        ← REST endpoints
├── service/
│   ├── RoadService.java           ← Core business logic
│   └── MLService.java             ← Calls Flask/YOLO
├── model/
│   └── PotholeReport.java         ← JPA entity
├── dto/
│   └── RoadDTO.java               ← Request/Response DTOs
├── repository/
│   └── PotholeReportRepository.java ← DB queries (Haversine)
└── exception/
    └── GlobalExceptionHandler.java  ← Error handling
```

---

## Setup

### 1. PostgreSQL — Create Database

```sql
CREATE DATABASE vanetdb;
CREATE USER vanet_user WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE vanetdb TO vanet_user;
```

### 2. Update application.properties

```properties
spring.datasource.username=vanet_user
spring.datasource.password=yourpassword
```

### 3. Run the app

```bash
mvn spring-boot:run
```

Hibernate will auto-create the `pothole_reports` table on first run.

---

## API Endpoints

### POST /api/road/share
Report a pothole from a vehicle.

**Request:**
```json
{
  "latitude": 16.823,
  "longitude": 75.126,
  "severity": "HIGH",
  "imageData": "base64encodedimage...",
  "reportedBy": "vehicle-001"
}
```

**Response:**
```json
{
  "id": 1,
  "latitude": 16.823,
  "longitude": 75.126,
  "severity": "HIGH",
  "mlConfidence": 0.91,
  "confirmationCount": 1,
  "verified": false,
  "reportedBy": "vehicle-001",
  "reportedAt": "2026-04-07T10:30:00"
}
```

---

### POST /api/road/nearby
Get all hazards within radius of current location.

**Request:**
```json
{
  "latitude": 16.823,
  "longitude": 75.126,
  "radiusKm": 2.0
}
```

---

### GET /api/road/all
Get all verified pothole reports for the map.

---

### GET /api/road/health
Health check — returns `200 OK`.

---

## Severity Levels

| Level    | Description                          |
|----------|--------------------------------------|
| LOW      | Minor surface crack                  |
| MEDIUM   | Visible pothole, manageable          |
| HIGH     | Deep pothole, dangerous for bikes    |
| CRITICAL | Road severely damaged, avoid area    |

---

## Confirmation Logic

- A new report starts with `confirmationCount = 1`
- If another vehicle reports a pothole within **50 metres**, the count increments
- When `confirmationCount >= 3` (configurable), the report is marked **verified**
- Only verified reports appear on the public map

---

## Next Steps (Phase 2)

- [ ] Add Web3j + Polygon smart contract integration
- [ ] Store images on IPFS, save hash in DB
- [ ] Add blockchain transaction hash to each verified report
- [ ] Add MetaMask wallet support in frontend
