import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";

export async function load({ fetch, depends, url, cookies }) {
  let sensorStationId = cookies.get("sensorStationId");

  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  if (!res.ok) {
    logger.error("Could not get sensor station");
    throw new error(res.status, "Could not get sensor station");
  }

  res = await res.json();
  logger.info("Got sensor station");
  depends("app:getSensorStation");
  return res;
}

export const actions = {
  update: async ({ cookies, request, fetch }) => {},

  setLimits: async ({ cookies, request, fetch }) => {
    let formData = await request.formData();
  },
};
