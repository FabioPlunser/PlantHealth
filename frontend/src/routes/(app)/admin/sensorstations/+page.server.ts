import { BACKEND_URL } from "$env/static/private";
import { error } from "@sveltejs/kit";

// TODO: add validation and error handling (toast messages)
export async function load({ locals, fetch, request, depends, url }) {
  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  if (!res.ok) {
    throw new error(res.status, "Could not get sensor stations");
  }
  res = await res.json();

  depends("app:getSensorStations");
  return {
    fromAccessPoints,
    sensorStations: res.sensorStations,
  };
  ü;
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
};
