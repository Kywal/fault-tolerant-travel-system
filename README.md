# Fault Tolerant Travel System

This repository contains a small example of a travel system composed of four microservices:

- `travel` — orchestrates ticket purchases and registers loyalty bonuses.
- `airlines` — provides flight information and processes ticket sales.
- `exchange` — provides currency conversion rates.
- `fidelity` — records user loyalty points / bonuses.

Requirements
------------

- Docker and Docker Compose
- (Optional) JDK and Maven to run services locally

Run the system
--------------

Start all services with Docker Compose:

```bash
docker compose up -d
```

Stop services:

```bash
docker compose down
```

Quick example
-------------

Make a purchase (example):

```bash
curl -s -X POST http://localhost:8080/buyTicket \
  -H "Content-Type: application/json" \
  -d '{
    "flight": "LA8084",
    "day": "2025-11-15",
    "user": "joao.silva"
  }' | (echo -e "\n=== Result ===" && cat && echo -e "\n================")
```

Expected output:

```text
=== Result ===
Compra realizada! Transaction ID: <uuid> | Valor: R$ <value>
================
```

Fault-tolerance strategies
---------------------------

The system implements four main fault-tolerance strategies across the microservices:

1. **Retry with Exponential Backoff** (Airlines Service)
   - Request: GET /flight
   - Behavior: Automatically retries up to 3 times with increasing delays (1s, 2s, 3s) when the flight consultation fails
   - Applicable when: Service is temporarily unavailable (omission faults)

2. **Circuit Breaker** (Travel Service)
   - Request: POST /sell
   - Behavior: Monitors the Airlines POST /sell endpoint and blocks requests after 3 consecutive failures. Attempts recovery after 30 seconds in HALF_OPEN state
   - Applicable when: Service degradation needs to be detected and mitigated proactively

3. **Fallback with History** (Exchange Service)
   - Request: GET /convert
   - Behavior: If currency conversion fails, uses the average of the last 10 exchange rates from history. Falls back to default rate (5.5) if no history exists
   - Applicable when: Approximate values are acceptable during temporary outages

4. **Async Queue Retry** (Fidelity Service)
   - Request: POST /bonus (bonus registration)
   - Behavior: If registration fails and fault tolerance is enabled, enqueues the request and retries every 10 seconds in the background
   - Applicable when: Non-critical operations can be deferred

Fault-tolerance parameter `ft`
-----------------------------

The `travel` service supports a per-request query parameter named `ft` (fault-tolerance). Below is a complete explanation of how it works.

- Accepted values: `true` or `false` (default is `false`).

Behavior

- `ft=false` (default): `travel` applies only retry logic on flight consultation. If any other request fails (sell, exchange, or bonus registration), the entire purchase fails
- `ft=true`: `travel` activates all fault-tolerance strategies. Retries are applied, circuit breaker protects against cascading failures, fallback values are used when services are unavailable, and failed bonus registrations are queued for later processing

How to use (examples)
----------------------

Enable fault tolerance:

```bash
curl -s -X POST "http://localhost:8080/buyTicket?ft=true" \
  -H "Content-Type: application/json" \
  -d '{
    "flight": "LA8084",
    "day": "2025-11-15",
    "user": "joao.silva"
  }' | (echo -e "\n=== Result ===" && cat && echo -e "\n================")
```

Disable fault tolerance:

```bash
curl -s -X POST "http://localhost:8080/buyTicket?ft=false" \
  -H "Content-Type: application/json" \
  -d '{
    "flight": "LA8084",
    "day": "2025-11-15",
    "user": "joao.silva"
  }' | (echo -e "\n=== Result ===" && cat && echo -e "\n================")
```


How to force failures in `fidelity` for testing
-----------------------------------------------

- Quick mode (temporary): edit `fidelity/src/main/java/.../FidelityController.java` and set the `FaultConfig` probability to `1.0` (100%), then rebuild the container:

```bash
docker compose up -d --build fidelity
```

- Controlled mode (if available): use `?force=true` on the `/bonus` endpoint to force a failure only when desired.

Simulated fault types
---------------------

The system can simulate four types of faults to test different failure scenarios:

- **Omission** - The service does not respond to the request. The client receives no response and times out
- **Error** - The service returns an HTTP 500 error. Useful for testing error handling and fallback mechanisms
- **Time** - The service responds after an artificial delay. Simulates slow network or overloaded services
- **Crash** - The service process terminates and must be restarted. Tests recovery mechanisms like container restarts

Each microservice has fault injection configured for specific endpoints:

| Service | Endpoint | Fault Type | Probability | Delay |
|---------|----------|-----------|-----------|--------|
| Airlines | GET /flight | Omission | 20% | - |
| Airlines | POST /sell | Time | 10% | 10s |
| Exchange | GET /convert | Error | 10% | 5s |
| Fidelity | POST /bonus | Crash | 2% | - |

These probabilities can be adjusted in the `FaultConfig` definitions within each service's controller to simulate different failure rates and scenarios. When a fault is triggered and has a duration, it remains active for the specified seconds, affecting subsequent requests until the duration expires.