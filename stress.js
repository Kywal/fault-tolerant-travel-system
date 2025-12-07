import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

export const options = {
  stages: [
    { duration: "1m", target: 50 },
    { duration: "2m", target: 100 }, // Carga alta
    { duration: "2m", target: 200 }, // Carga muito alta
    { duration: "1m", target: 0 },
  ],
  thresholds: {
    availability_rate: ["rate>0.70"],
  },
};

const availability_rate = new Rate("availability_rate");

export default function () {
  const url = "http://localhost:8080/buyTicket";
  const payload = JSON.stringify({
    flight: "LA8084",
    day: "2025-11-15",
    user: "joao.silva",
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
    timeout: "20s",
  };

  const res = http.post(url, payload, params);

  const result = check(res, {
    "status is 200": (r) => r.status === 200,
  });

  availability_rate.add(result);

  sleep(1);
}
