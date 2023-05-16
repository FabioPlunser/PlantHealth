import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

export async function getSensorStationData(
  event: any,
  fetch: any,
  sensorStation: any,
  from: any,
  to: any
) {
  sensorStation.data = new Promise(async (resolve, reject) => {
    await fetch(
      `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
        sensorStation.sensorStationId
      }&from=${from.toISOString().split(".")[0]}&to=${
        to.toISOString().split(".")[0]
      }`
    )
      .then(async (res: any) => {
        let data = await res.json();
        resolve(data);
      })
      .catch((e: any) => {
        logger.error("Error while fetching sensor station data", { e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching sensor station data"
        );
        reject(e);
      });
  });
}
