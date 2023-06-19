import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

/**
 * This is an async function that unlocks a sensor station by sending a POST request to a backend URL
 * with the sensor station ID and unlocked status as parameters.
 * @param {any} event - The event parameter is an object that contains information about the HTTP
 * request that triggered the function, including the request object and the fetch function that can be
 * used to make HTTP requests.
 */
export async function unlockSensorStation(event: any) {
  const { request, fetch } = event;
  const formData = await request.formData();
  let sensorStationId = String(formData.get("sensorStationId"));
  let unlocked = String(formData.get("unlocked"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);
  params.set("unlocked", unlocked);

  await fetch(
    `${BACKEND_URL}/set-unlocked-sensor-station?${params.toString()}`,
    {
      method: "POST",
    }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Couldn't unlock sensor station",
          await res.json()
        );
      } else {
        logger.info(
          `Sensor station unlocked set to '${unlocked}' for id = ${sensorStationId}`
        );
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          unlocked === "true"
            ? "sensor station unlocked"
            : "sensor station locked"
        );
      }
    })
    .catch((err: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Couldn't unlock sensor station",
        err
      );
      throw error(500, "Couldn't unlock sensor station");
    });
}
