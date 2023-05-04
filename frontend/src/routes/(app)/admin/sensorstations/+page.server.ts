import { BACKEND_URL } from "$env/static/private";

// TODO: add validation and error handling (toast messages)
export async function load({ locals, fetch, request, depends }) {
  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  console.log(res);
  res = await res.json();
  console.log(res);

  depends("app:getSensorStations");
  return {
    sensorStations: res.sensorStations,
  };
}

export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let unlocked = formData.get("unlocked");
    let res = await fetch(
      `${BACKEND_URL}/set-unlocked-sensor-station?sensorStationId=${sensorStationId}&unlocked=${unlocked}`,
      {
        method: "POST",
      }
    );
  },

  // TODO: add validation and error handling (toast messages)
  update: async ({ cookies, request, fetch }) => {},

  // TODO: add validation and error handling (toast messages)
  scan: async ({ cookies, request, fetch }) => {},

  delete: async ({ cookies, request, fetch }) => {
    console.log("delete");
  },
} satisfies Actions;
