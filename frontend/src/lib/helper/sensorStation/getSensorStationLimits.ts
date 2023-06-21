import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This is an asynchronous function that fetches sensor station limits from a backend URL and returns a
 * promise that resolves with the limits data.
 * @param {any} event - The event parameter is an object that is used to make HTTP requests to a
 * backend server. It is likely an instance of the Fetch API or a similar library.
 * @param {SensorStation} sensorStation - The `sensorStation` parameter is an object that represents a
 * sensor station. It contains a `sensorStationId` property that is used to fetch the sensor station's
 * limits from the backend.
 * @returns The function `getSensorStationLimits` returns a Promise that resolves to the `sensorLimits`
 * property of the `data` object fetched from the backend API endpoint. If there is an error during the
 * fetch or Promise rejection, an error message is thrown.
 */
export async function getSensorStationLimits(
  event: any,
  sensorStation: Responses.SensorStationBaseResponse
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
  });
}
