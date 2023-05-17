import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { z } from "zod";
import { toasts } from "$lib/stores/toastStore";

export async function load(event) {
  const { fetch, request, url, depends } = event;
  depends("app:getSensorStations");

  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  async function getAllSensorStations(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-all-sensor-stations`)
        .then(async (res: any) => {
          if (!res.ok) {
            logger.error("Could not get all sensor stations");
            throw error(500, "Could not get all sensor stations");
          }
          let data = await res.json();
          resolve(data.sensorStations);
        })
        .catch((err) => {
          logger.error("Catch Could not get all sensor stations", {
            payload: err,
          });
          throw error(500, "Catch Could not get all sensor stations");
        });
    }).catch((err) => {
      logger.error("Could not get sensor stations");
      throw error(500, "Could not get sensor stations");
    });
  }

  async function getGardener(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-all-gardener`)
        .then(async (res) => {
          if (!res.ok) {
            logger.error("Could not get gardener");
            throw error(500, "Could not get gardener");
          }
          let data = await res.json();
          resolve(data.items);
        })
        .catch((err) => {
          logger.error("Could not get gardener");
          reject(err);
          throw error(500, "Could not get gardener");
        });
    }).catch((err) => {
      logger.error("Could not get gardener");
      throw error(500, "Could not get gardener");
    });
  }

  return {
    fromAccessPoints,
    gardener: getGardener(),
    streamed: {
      sensorStations: getAllSensorStations(),
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
