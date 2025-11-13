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

Fault-tolerance parameter `ft`
-----------------------------

The `travel` service supports a per-request query parameter named `ft` (fault-tolerance). Below is a complete explanation of how it works today and recommendations for use.

- Accepted values: `true` or `false` (default is `false`).

Behavior

- `ft=false` (default): `travel` attempts to register the bonus with `fidelity`. If the call fails, the purchase fails and an error is returned to the client.
- `ft=true`: `travel` completes the sale even if the call to `fidelity` fails. The failed bonus is enqueued in an in-memory queue and retried automatically in the background.

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

