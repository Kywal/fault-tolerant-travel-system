import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";

export const options = {
  vus: 1,
  duration: "30s",
  thresholds: {
    availability_rate: ["rate>0.95"],
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
