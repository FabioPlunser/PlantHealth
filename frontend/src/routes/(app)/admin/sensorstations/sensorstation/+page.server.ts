import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch, depends }) {
  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  res = await res.json();

  depends("app:getSensorStations");
  return {
    sensorStations: res.sensorStations,
  };
}
