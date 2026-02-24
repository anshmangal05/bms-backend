# Booking Management System (BMS)

A Spring Boot based event-driven backend system designed to manage customer bookings and policies using Kafka for asynchronous communication and MongoDB for persistence.

This system follows microservice-friendly architecture principles and demonstrates real-world backend engineering concepts.

#  Project Theory

The Booking Management System (BMS) is built using an event-driven architecture.

Instead of tightly coupling services together, the system uses Apache Kafka to publish and consume events. This makes the system:

- Scalable
- Loosely coupled
- Fault tolerant
- Suitable for microservices architecture

MongoDB is used as a NoSQL database to store customer and policy data.


# High-Level Architecture

Client (REST API)
        ↓
Controller Layer
        ↓
Service Layer
        ↓
Kafka Producer → Kafka Topic → Kafka Consumer
        ↓
MongoDB Repository
        ↓
Database (MongoDB)

##  Customer Booking Flow

1. Client sends booking request via REST API.
2. Controller receives request.
3. Service validates business logic.
4. Kafka Producer publishes booking event.
5. Kafka Consumer listens to topic.
6. Consumer processes event.
7. Data is stored in MongoDB.
8. Response returned to client.

---

## Policy Update Flow

1. Client sends policy update request.
2. Service generates PolicyUpdateEvent.
3. Event is published to Kafka topic.
4. Consumer receives event.
5. Repository updates MongoDB document.
6. Success response returned.

---

#  Kafka Event Flow

Kafka Topics Used:
- policy-add
- policy-update
- policy-delete

Producer:
- Publishes events (PolicyAddEvent, PolicyUpdateEvent, etc.)

Consumer:
- Listens to configured topics
- Deserializes JSON events
- Performs database operations

---

# Why Event-Driven Architecture?

Traditional synchronous calls create tight coupling.

With Kafka:
- Services don’t depend directly on each other
- System becomes more resilient
- Failures can be retried
- High throughput handling

This architecture is suitable for:
- Banking systems
- Booking platforms
- E-commerce systems
- Distributed microservices

---

#  Tech Stack

- Java 17
- Spring Boot 3
- Spring Kafka
- MongoDB
- Maven
- Lombok

#  Project Structure

com.example.bms
│
├── configuration     → Kafka & Mongo configuration
├── controller        → REST Controllers
├── model             → Entities & Event models
├── repository        → MongoDB repositories
├── service           → Business logic
│   └── kafka         → Kafka Producer & Consumer
├── utility           → Utility classes
└── validation        → Validation logic

---

#  Design Principles Used

- Layered Architecture
- Event-Driven Architecture
- Repository Pattern
- Producer-Consumer Pattern
- Dependency Injection
- Separation of Concerns

