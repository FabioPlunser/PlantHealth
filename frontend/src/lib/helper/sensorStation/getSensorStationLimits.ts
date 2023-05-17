import { BACKEND_URL } from "$env/static/private";
import { ErrorHandler } from "../errorHandler";

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
        let data = await res.json();
        resolve(data.sensorStation.sensorLimits);
      })
      .catch((e: any) => {
        ErrorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station limits",
          e
        );
        reject(e);
      });
  });
}
