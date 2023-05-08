import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch, depends, url, cookies }) {
  let sensorStationId = cookies.get("sensorStationId");

  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  res = await res.json();

  depends("app:getSensorStation");
  return res;
}

export const actions = {
  update: async ({ cookies, request, fetch }) => {},

  setLimits: async ({ cookies, request, fetch }) => {
    console.log("setLimits");
    let formData = await request.formData();
    console.log("formData", formData);
  },
};
