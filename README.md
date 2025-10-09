# 🚕 Cab Aggregator (Microservice Taxi Platform)

**Technologies:** Spring Boot · Java 17 · PostgreSQL · Kafka · Feign · Eureka · Docker · Testcontainers · Liquibase

---

## 🧩 Overview
Cab Aggregator is a **microservice-based taxi platform** project, this repo contains part, which developed by me. 
It connects **drivers and passengers**, providing ride management, rating calculation, and real-time synchronization between services.

---

## ⚙️ Architecture

### 🧱 Microservices
- **Driver Service** – manages driver profiles, availability, and ride assignments  
- **Ride Service** – handles ride lifecycle (request, start, complete, cancel)  
- **Rating Service** – aggregates driver ratings and feedback  
- **Passenger Service** – passenger registration, trip history  
- **Eureka Server** – service discovery & registry  

Each service is a standalone Spring Boot application with its own database and configuration.

### 🔄 Communication
- **REST + Feign Clients** for synchronous inter-service calls  
- **Kafka topics** for asynchronous communication (ride and rating events)  
- **Eureka Server** for dynamic service discovery  

---

## 🛠️ Technologies

| Layer | Stack |
|-------|--------|
| **Backend Framework** | Spring Boot 3.3.3, Spring Web, Spring Data JPA |
| **Persistence** | PostgreSQL, Liquibase |
| **Mapping & Validation** | MapStruct, Jakarta Validation, Hibernate Validator |
| **Inter-Service** | OpenFeign, Spring Cloud, Eureka |
| **Messaging** | Apache Kafka |
| **Testing** | JUnit 5, Mockito, Cucumber, WireMock, Rest-Assured, Testcontainers, Spring Cloud Contract |
| **Build & Deployment** | Maven, Docker |

---

## 🧪 Testing Strategy
The project applies **multi-layer testing**:
- **Unit tests** (Mockito)
- **Integration tests** (Spring Boot Test + Testcontainers)
- **Contract tests** (Spring Cloud Contract + WireMock)
- **End-to-end scenarios** (Cucumber)

---

## 🔍 How to See Source Code

As project developed using **GitFlow Workflow**, all source is located under **develop** branch.<br/>

Also, any project function available under **feature branch**.

## 🧭 How to Run Locally

```bash
# Start all services and dependencies
docker-compose up
```

Each module is independent and can be run individually:

```bash
cd src/driver-service
mvn spring-boot:run
```

---

## 🧠 Future Improvements  
- Implement **Notification Service** for real-time updates  
- Extend **Keycloak-based security** to all modules  

---

👨‍💻 *Developed by [Fyodor Saprankov](https://github.com/FedorDeveloper)*

