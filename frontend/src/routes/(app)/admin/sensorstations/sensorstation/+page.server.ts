import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch, depends, url }) {
  let sensorStationId = url.searchParams.get("sensorStationId");
  console.log("sensorStationId", sensorStationId);
  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  res = await res.json();

  depends("app:getSensorStation");
  return {
    sensorStation: res.sensorStation,
  };
}
