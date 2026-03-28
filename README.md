# Biddex

**Biddex** B2B tender və hərrac platformasının mikroservis arxitekturası. Layihə Java (Spring Boot / Spring Cloud), Python (FastAPI), React və müxtəlif məlumat saxlama və mesajlaşma komponentləri üzərində qurulur.

## İnfrastrukturun işə salınması

Layihə kökündə:

```bash
docker compose up -d
```

Bu əmr PostgreSQL, MongoDB, Redis, Zookeeper, Kafka, MinIO, Zipkin, Elasticsearch, Kibana və Logstash konteynerlərini `biddex-network` şəbəkəsində qaldırır. Spring Boot mikroservisləri bu faylda təyin olunmur; onlar öz `Dockerfile` faylları ilə ayrıca build və run edilir.

İlk dəfə `postgres-tender` qalxanda `docker/postgres-tender/init` altındakı skriptlər `biddex_company` və `biddex_report` verilənlər bazalarını da yaradır (`biddex_tender` əsas DB kimi qalır).

## Servis portları

| Komponent | Port | Qeyd |
|-----------|------|------|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8080 | Spring Cloud Gateway (WebMVC) |
| auth-service | 8081 | PostgreSQL `biddex_auth` (host: 5433) |
| company-service | 8082 | PostgreSQL `biddex_company` (host: 5434) |
| tender-service | 8083 | PostgreSQL `biddex_tender`, Redis, Kafka |
| bidding-service | 8084 | MongoDB `biddex_bidding`, Redis, Kafka |
| notification-service | 8085 | Kafka, mail (konfiqurasiya oluna bilər) |
| chat-service | 8086 | MongoDB `biddex_chat` |
| report-service | 8087 | PostgreSQL `biddex_report` |
| analytics-service | 8090 | FastAPI (Dockerfile / `uvicorn`) |
| postgres-auth | 5433 | DB: `biddex_auth` |
| postgres-tender | 5434 | DB: `biddex_tender`, `biddex_company`, `biddex_report` |
| MongoDB | 27017 | |
| Redis | 6379 | |
| Zookeeper | 2181 | |
| Kafka | 9092 | PLAINTEXT (`localhost`), broker daxili `kafka:29092` |
| MinIO S3 | 9000 | Konsol: 9001 (istifadəçi/parol: `biddex` / `biddexsecret`) |
| Zipkin | 9411 | Distributed tracing |
| Elasticsearch | 9200 | Tək node |
| Kibana | 5601 | |
| Logstash TCP (JSON lines) | 5044 | `logback-spring.xml` appender |

## Texnologiyalar

- **Java 21**, **Spring Boot 3.5.x**, **Spring Cloud 2025.x**
- **Spring Cloud Gateway**, **Netflix Eureka**
- **Apache Kafka** (hadisə axını)
- **PostgreSQL** (auth, company, tender, report üçün uyğun sxemlər)
- **MongoDB** (bidding, chat)
- **Redis** (keş, JWT blacklist, rate limiting üçün hazırlıq)
- **MinIO** (fayl saxlama)
- **Micrometer + Brave + Zipkin** (izləmə)
- **Logstash** (mərkəzləşdirilmiş log; TCP 5044)
- **Python 3.11**, **FastAPI**, **scikit-learn**, **pandas**, **SQLAlchemy**, **psycopg2-binary**, **kafka-python**

## Spring servislərin Docker ilə build-i

Hər modul öz alt qovluğundadır, məsələn:

```bash
docker build -t biddex-auth-service -f services/auth-service/auth-service/Dockerfile services/auth-service/auth-service
```

Eyni şəkildə `eureka-server`, `api-gateway`, `company-service`, `tender-service`, `bidding-service`, `notification-service`, `chat-service`, `report-service` üçün uyğun yolları dəyişin.

## analytics-service

```bash
cd services/analytics-service
docker build -t biddex-analytics-service .
```

Və ya lokal:

```bash
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8090
```

## Frontend

`frontend/` — React, TypeScript, Ant Design (ayrıca build və konfiqurasiya).
