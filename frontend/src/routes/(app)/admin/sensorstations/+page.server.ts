import { BACKEND_URL } from "$env/static/private";
import { error } from "@sveltejs/kit";
import { logger } from "$helper/logger";

// TODO: add validation and error handling (toast messages)
/**
 * This function loads sensor stations from a backend URL and returns them along with a boolean
 * indicating whether the request was made from an access points page.
 * @param  - - `locals`: an object containing local variables for the current request
 * @returns An object with two properties: "fromAccessPoints" and "sensorStations". The
 * "fromAccessPoints" property is a boolean value indicating whether the request was made from the
 * "/admin/accesspoints" page. The "sensorStations" property is an array of sensor station objects
 * obtained from a fetch request to the backend API.
 */
export async function load({ locals, fetch, request, depends, url }) {
  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  if (!res.ok) {
    logger.error("Could not get sensor stations");
    throw new error(res.status, "Could not get sensor stations");
  }
  res = await res.json();
  logger.info("Got sensor stations");

  depends("app:getSensorStations");
  return {
    fromAccessPoints,
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
    if (!res.ok) {
      logger.error("Could not unlock sensor station");
      throw new error(res.status, "Could not unlock sensor station");
    } else {
      logger.info("Unlocked sensor station");
    }
  },

  // TODO: add validation and error handling (toast messages)
  update: async ({ cookies, request, fetch }) => {},

  // TODO: add validation and error handling (toast messages)
  scan: async ({ cookies, request, fetch }) => {},

  delete: async ({ cookies, request, fetch }) => {},
};
