import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

export async function getSensorStationLimits(
  event: any,
  sensorStation: SensorStation
) {
  return new Promise(async (resolve, reject) => {
    await event
      .fetch(
        `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStation.sensorStationId}`
      )
      .then(async (res: any) => {
        if (!res.ok) {
          throw error(500, "Error while fetching sensor station limits");
        }
        let data = await res.json();
        resolve(data.sensorStation.sensorLimits);
      })
      .catch((e: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station limits",
          e
        );
        reject(e);
      });
  }).catch((e: any) => {
    errorHandler(
      event.locals.user?.personId,
      "Error while fetching sensor station limits",
      e
    );
    throw error(500, "Error while fetching sensor station limits");
  });
}
