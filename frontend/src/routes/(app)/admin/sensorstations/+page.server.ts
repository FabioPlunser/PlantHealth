import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { z } from "zod";
import { toasts } from "$lib/stores/toastStore";

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
export async function load({ fetch, request, depends, url }) {
  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  depends("app:getSensorStations");

  let getSensorStations = await fetch(`${BACKEND_URL}/get-all-sensor-stations`)
    .then(async (res) => {
      if (!res.ok) {
        logger.error("Could not get sensor stations");
        throw error(res.status, "Could not get sensor stations");
      }
      let data = await res.json();
      return data.sensorStations;
    })
    .catch((err) => {
      logger.error("Could not get sensor stations");
      throw error(500, "Could not get sensor stations");
    });

  let gardener = await fetch(`${BACKEND_URL}/get-all-gardener`)
    .then(async (res) => {
      if (!res.ok) {
        logger.error("Could not get gardener");
        throw error(res.status, "Could not get gardener");
      }
      let data = await res.json();
      return data.items;
    })
    .catch((err) => {
      logger.error("Could not get gardener");
      throw error(500, "Could not get gardener");
    });

  return {
    fromAccessPoints,
    gardener,
    streamed: {
      sensorStations: getSensorStations,
    },
  };
}

//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
import {
  deleteSensorStation,
  updateSensorStation,
  unlockSensorStation,
} from "$helper/actions";

export const actions = {
  unlock: async (event) => {
    await unlockSensorStation(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  update: async (event) => {
    await updateSensorStation(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  delete: async ({ request, fetch, locals }) => {
    await deleteSensorStation({ request, fetch, locals });
  },
};
