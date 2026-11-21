# Dependencies

A Maven **monolith** for shared Spring Boot building blocks and a small, from-scratch pub/sub messaging stack.

Java 21 · Spring Boot 4.1.1 · Maven multi-module

This repo is one GitHub repository with three modules that you build together:

| Module | Kind | Role |
| --- | --- | --- |
| `common-spring-boot-starter` | Shared starter | Opinionated baseline: web, JPA, validation, OpenAPI, Eureka client, Redis sessions, H2 |
| `custom-messaging` | Client library | Spring Boot auto-config for `Publisher` / `Subscriber` against the broker HTTP API |
| `custom-messaging-broker` | Runnable service | In-memory topic broker with per-subscriber queues |

The broker is a teaching-scale Kafka-style *shape*: create a topic, register subscribers, publish a payload, each subscriber gets its own copy. It is **not** a production message bus (no persistence, no auth, no clustering).

---

## Architecture

```
┌─────────────────────────┐
│  Your Spring Boot app   │
│  (depends on            │
│   custom-messaging)     │
│                         │
│  Publisher ─────────┐   │
│  Subscriber ────────┤   │
└─────────────────────┼───┘
                      │ HTTP
                      ▼
        ┌─────────────────────────────┐
        │ custom-messaging-broker     │
        │ :9090                       │
        │                             │
        │  Topic → Map of subscriber  │
        │          BlockingQueues     │
        └─────────────────────────────┘
```

- **Fan-out:** `Topic.publish` copies the same `Message` onto every subscriber queue.
- **Long poll:** `GET .../messages` blocks on `queue.take()` until a message arrives.
- **Client consume loop:** `Subscriber` registers, then polls in a background thread and invokes your `Consumer<Message>`.

---

## Repository layout

```
.
├── pom.xml                          # aggregator parent
├── common-spring-boot-starter/
├── custom-messaging/
│   └── AutoConfig + Publisher/Subscriber
└── custom-messaging-broker/
    └── REST API + in-memory MessageBroker
```

Build everything from the repo root (Maven 3.9+ on your `PATH`, or a module `mvnw` / `mvnw.cmd`):

```bash
mvn clean verify
```

Windows, from a module that still has the wrapper:

```powershell
.\custom-messaging-broker\mvnw.cmd -f ..\pom.xml clean verify
```

---

## Prerequisites

- JDK **21**
- Maven **3.9+** (or a module `mvnw` / `mvnw.cmd`)
- No extra infrastructure for the broker (queues live in process memory)

---

## Run the broker

```bash
mvn -pl custom-messaging-broker spring-boot:run
```

Default port: **9090** (`custom-messaging-broker/src/main/resources/application.properties`).

### HTTP API

| Method | Path | Behavior |
| --- | --- | --- |
| `POST` | `/topics/{topicName}` | Create topic (idempotent `putIfAbsent`) |
| `POST` | `/topics/{topicName}/messages` | Publish `{ "payload": ... }` to all subscribers |
| `POST` | `/topics/{topicName}/subscribers/{subscriberId}` | Register a subscriber queue |
| `GET`  | `/topics/{topicName}/subscribers/{subscriberId}/messages` | **Blocking** take of the next message |

Quick smoke test (create → subscribe → publish → consume):

```bash
curl -X POST http://localhost:9090/topics/orders

curl -X POST http://localhost:9090/topics/orders/subscribers/inventory-service

curl -X POST http://localhost:9090/topics/orders/messages ^
  -H "Content-Type: application/json" ^
  -d "{\"payload\":{\"orderId\":42}}"

curl http://localhost:9090/topics/orders/subscribers/inventory-service/messages
```

On bash, drop the `^` line continuations.

---

## Use the client library

Add the module after `mvn -pl custom-messaging install`:

```xml
<dependency>
    <groupId>org.dep</groupId>
    <artifactId>custom-messaging</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Auto-config registers `Publisher` and `Subscriber` beans. Point them at the broker:

```properties
custom.messaging.broker-url=http://localhost:9090
```

```java
publisher.publish("orders", Map.of("orderId", 42));

subscriber.subscribe("orders", "inventory-service", message -> {
    // handle message.payload()
});
```

Spring Boot 3+ auto-configuration is declared in:

`custom-messaging/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

---

## Shared starter

`common-spring-boot-starter` packages the stack you want every service to inherit (web, JPA, validation, springdoc OpenAPI, Eureka client, Redis sessions, H2). Consume it the same way as any other starter after installing it to your local Maven repo.

Do not treat it as a finished microservice: it currently ships a default `@SpringBootApplication` class so the module can boot for local experiments.

---

## Security (keep secrets out of Git)

This project is public-ready **only if** local secrets never land in the repo.

Already ignored at the monolith root:

- `.env`, `.env.*` (except `.env.example`)
- `*.pem`, `*.p12`, `*.jks`, keystores, SSH keys
- `application-local.properties`, `application-secret.properties`
- `credentials.json`, `secrets.json`
- `target/`, `.idea/`, IDE metadata

Rules:

1. Copy `.env.example` → `.env` and keep `.env` on disk only.
2. Prefer environment variables or a secret manager over hardcoded passwords.
3. The broker has **no authentication**. Bind it to localhost for demos. Do not expose `:9090` on the public internet.
4. `GET .../messages` holds the HTTP thread until a message arrives. That is fine for a lab; it is a denial-of-service risk if left open.

If you ever committed a secret, rotate it. Git history still contains it even after you delete the file.

---

## Design notes

**Why HTTP instead of a binary protocol?**  
The client (`RestClient`) and broker stay easy to debug with curl. The blocking GET is a simple stand-in for a consumer group fetch.

**Why per-subscriber `LinkedBlockingQueue`?**  
Each subscriber must see every published message (pub/sub), not compete for one shared queue (work queue).

**Concurrency.**  
Topics live in a `ConcurrentHashMap`. Publish walks subscriber queues and `put`s. Consumers `take`. That is enough for a single-process broker.

**What this is not.**  
No disk log, no acknowledgements, no retries with backoff, no TLS, no multi-instance consensus. Use Kafka, RabbitMQ, or Pulsar when those matter.

---

## License

MIT. See [LICENSE](LICENSE).
