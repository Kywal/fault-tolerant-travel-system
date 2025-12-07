import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const taxaSucesso = new Rate("taxa_sucesso");
const duracaoRequisicao = new Trend("duracao_req");

export const options = {
  scenarios: {
    // 1. Fase de aquecimento (0s a 1m)
    fase_aquecimento: {
      executor: "constant-vus",
      vus: 5,
      duration: "1m",
      startTime: "0s",
      tags: { stage: "warm_up" },
    },
    // 2. Fase de carga pesada (1m a 4m)
    fase_carga: {
      executor: "constant-vus",
      vus: 50,
      duration: "3m",
      startTime: "1m",
      tags: { stage: "load" },
    },
    // 3. Fase de resfriamento (4m a 5m)
    fase_resfriamento: {
      executor: "constant-vus",
      vus: 5,
      duration: "1m",
      startTime: "4m",
      tags: { stage: "cool_down" },
    },
  },
  thresholds: {
    // 1. Métricas para o AQUECIMENTO
    "http_req_duration{stage:warm_up}": ["p(95)<1000"],
    "taxa_sucesso{stage:warm_up}": ["rate>0.99"],

    // 2. Métricas para a CARGA (O foco do teste)
    "http_req_duration{stage:load}": ["p(95)<6000"], // Aceitamos até 6s (devido ao erro de 5s)
    "taxa_sucesso{stage:load}": ["rate>0.80"],

    // 3. Métricas para o RESFRIAMENTO
    "http_req_duration{stage:cool_down}": ["p(95)<1000"],
    "taxa_sucesso{stage:cool_down}": ["rate>0.99"],
  },
};

export default function () {
  const url = "http://localhost:8080/buyTicket";
  const payload = JSON.stringify({
    flight: "VOO-1234",
    day: "2025-10-20",
    "user-id": "user_test",
  });

  const params = {
    headers: { "Content-Type": "application/json" },
    timeout: "20s",
  };

  const res = http.post(url, payload, params);

  const sucesso = check(res, {
    "status 200": (r) => r.status === 200,
  });

  taxaSucesso.add(sucesso);
  duracaoRequisicao.add(res.timings.waiting);

  sleep(1);
}
