# Fault Tolerant Travel System

To start the application you only need to have Docker installed. If so, run:
```bash
docker compose up -d
```

To see it in action, run this _curl_ command, and feel free to change the data in the json fields:
```bash
curl -s -X POST http://localhost:8080/buyTicket \
  -H "Content-Type: application/json" \
  -d '{
    "flight": "LA8084",
    "day": "2025-11-15",
    "user": "joao.silva"
  }' | (echo -e "\n=== Result ===" && cat && echo -e "\n================")
```
It should print something like this:
```text
=== Resultado ===
Compra realizada! Transaction ID: abbabaa6-6ef3-4100-87b3-a068c101220b | Valor: R$ 2509.29
================
```

To stop it, use:
```bash
docker compose down
```


---
### This travel system is made of four microservices:
- Travel (Manages the entire ticket purchase process)
- Airlines (Manages the flights and tickets sales)
- Exchange (Calculates currency conversions)
- Fidelity (Manages the loyalty points system)