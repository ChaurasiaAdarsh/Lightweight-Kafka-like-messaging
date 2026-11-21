
# Lightweight Kafka-Like Messaging System

A lightweight, Kafka-inspired messaging system built using **Java 21, Spring Boot, and Maven**. This project includes a custom in-memory message broker, a client library for publishing and subscribing to messages, and a shared Spring Boot starter for reusable dependencies.

The project is structured as a Maven multi-module repository with three modules:

| Module | Description |
|---|---|
| `common-spring-boot-starter` | A shared Spring Boot starter containing commonly used dependencies such as Web, JPA, Validation, OpenAPI, Eureka Client, Redis Sessions, and H2. |
| `custom-messaging` | A client library that provides `Publisher` and `Subscriber` components with Spring Boot auto-configuration. |
| `custom-messaging-broker` | A standalone messaging broker that manages topics, subscribers, and messages using in-memory queues. |

The messaging system follows the basic Kafka-style publish-subscribe approach. Messages published to a topic are delivered independently to all subscribers registered to that topic.

**Tech Stack:** Java 21 · Spring Boot 4.1.1 · Maven · REST API · ConcurrentHashMap · LinkedBlockingQueue

---

## Architecture

```text
┌─────────────────────────┐
│    Spring Boot App      │
│                         │
│  Publisher              │
│  Subscriber             │
│                         │
│  custom-messaging       │
└────────────┬────────────┘
             │
             │ HTTP
             ▼
┌─────────────────────────────┐
│   custom-messaging-broker   │
│                             │
│   Topic Management          │
│   Subscriber Registration  │
│   Message Distribution     │
│                             │
│   LinkedBlockingQueue      │
└─────────────────────────────┘
```

### How It Works

- **Topic Management:** Topics are created dynamically to organize messages.
- **Subscriber Registration:** Each subscriber gets its own message queue.
- **Message Publishing:** When a message is published, the broker distributes a copy to every registered subscriber.
- **Message Consumption:** Subscribers retrieve messages through a blocking HTTP endpoint.
- **Background Consumption:** The client library continuously polls for messages and passes them to the registered consumer.

---

## Project Structure

```text
.
├── pom.xml
├── common-spring-boot-starter/
├── custom-messaging/
│   └── Publisher, Subscriber & Auto-Configuration
└── custom-messaging-broker/
    └── REST API & In-Memory Message Broker
```

---

## Prerequisites

- JDK 21
- Maven 3.9+
- IDE such as IntelliJ IDEA or Eclipse (optional)

No external messaging infrastructure is required to run the broker.

## Build the Project

Clone the repository:

```bash
git clone https://github.com/ChaurasiaAdarsh/Lightweight-Kafka-like-messaging.git
```

Navigate to the project directory and build all modules:

```bash
mvn clean verify
```

---

## Running the Message Broker

Start the broker using:

```bash
mvn -pl custom-messaging-broker spring-boot:run
```

The broker runs on port **9090** by default.

### Available REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/topics/{topicName}` | Create a topic. |
| POST | `/topics/{topicName}/messages` | Publish a message to a topic. |
| POST | `/topics/{topicName}/subscribers/{subscriberId}` | Register a subscriber. |
| GET | `/topics/{topicName}/subscribers/{subscriberId}/messages` | Retrieve the next message from the subscriber's queue. |

### Testing the Messaging Flow

The following requests demonstrate the complete publish-subscribe flow.

**1. Create a topic**

```bash
curl -X POST http://localhost:9090/topics/orders
```

**2. Register a subscriber**

```bash
curl -X POST http://localhost:9090/topics/orders/subscribers/inventory-service
```

**3. Publish a message**

```bash
curl -X POST http://localhost:9090/topics/orders/messages \
-H "Content-Type: application/json" \
-d "{\"payload\":{\"orderId\":42}}"
```

**4. Consume the message**

```bash
curl http://localhost:9090/topics/orders/subscribers/inventory-service/messages
```

The subscriber receives the published message from its dedicated queue.

---

## Using the Client Library

The `custom-messaging` module provides an easy way to integrate the messaging system into other Spring Boot applications.

First, install the client library into your local Maven repository:

```bash
mvn -pl custom-messaging install
```

Add the following dependency to your Spring Boot application's `pom.xml`:

```xml
<dependency>
    <groupId>org.dep</groupId>
    <artifactId>custom-messaging</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Configure the broker URL in `application.properties`:

```properties
custom.messaging.broker-url=http://localhost:9090
```

### Publishing Messages

Inject the `Publisher` bean and publish messages to a topic:

```java
publisher.publish("orders", Map.of("orderId", 42));
```

### Subscribing to Messages

Use the `Subscriber` bean to register a subscriber and process incoming messages:

```java
subscriber.subscribe("orders", "inventory-service", message -> {
    System.out.println(message.payload());
});
```

The library automatically configures the `Publisher` and `Subscriber` beans through Spring Boot auto-configuration.

Auto-configuration is registered in:

```text
custom-messaging/src/main/resources/META-INF/spring/
org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## Common Spring Boot Starter

The `common-spring-boot-starter` module contains commonly used Spring Boot dependencies that can be shared across multiple applications.

It currently includes:

- Spring Web
- Spring Data JPA
- Validation
- Springdoc OpenAPI
- Eureka Client
- Redis Sessions
- H2 Database

Install the module into your local Maven repository and add it as a dependency wherever these shared configurations and dependencies are required.

---

## Design Decisions

**1. Why HTTP for communication?**

I used HTTP REST APIs to keep communication between the client library and broker simple and easy to test using tools like cURL.

**2. Why LinkedBlockingQueue?**

Each subscriber needs to receive its own copy of every published message. Using a separate `LinkedBlockingQueue` for each subscriber makes this possible without subscribers competing for messages.

**3. Why ConcurrentHashMap?**

Topics and their associated subscribers are maintained using `ConcurrentHashMap`, allowing concurrent access during message publishing and subscription operations.

**4. How is message consumption handled?**

The broker uses a blocking queue operation (`take()`) to wait until a message becomes available. The client library handles message consumption through a background thread.

---

## Current Limitations

This is a lightweight implementation intended for understanding and experimenting with messaging concepts. It is not a production-ready replacement for Apache Kafka.

Current limitations include:

- Messages are stored in memory and are lost when the broker restarts.
- No message persistence or acknowledgements.
- No retry mechanism or dead-letter queues.
- No authentication or authorization.
- No clustering or distributed broker support.
- Blocking HTTP requests are used for message consumption.

For production-grade messaging systems, technologies such as Apache Kafka, RabbitMQ, or Apache Pulsar are more suitable.

---

## License

This project is licensed under the [MIT License](LICENSE).
