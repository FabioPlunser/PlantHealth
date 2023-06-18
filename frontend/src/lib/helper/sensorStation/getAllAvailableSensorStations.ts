import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This function fetches all sensor stations from a backend URL and returns them as an array, while
 * also handling errors and getting the newest picture of the sensor stations.
 * @param {any} event - The `event` parameter is an object that represents the event that triggered the
 * function. It is likely an HTTP request object that contains information about the incoming request,
 * such as headers, query parameters, and request body.
 * @returns A Promise that resolves to an array of sensor stations.
 */
export async function getAllSensorStations(event: any): Promise<any> {
  return new Promise(async (resolve, reject) => {
    await event
      .fetch(`${BACKEND_URL}/get-sensor-stations`)
      .then(async (res: any) => {
        if (!res.ok) {
          let data = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while fetching all sensor stations",
            data
          );
          resolve({ sensorStations: [] });
        }

        let data = await res.json();
        let sensorStations = data?.sensorStations;
        if (sensorStations.lenght === 0) return [];
        //---------------------------------------------------------------------
        // get newest picture of sensor stations
        //---------------------------------------------------------------------
        getNewestPicture(event, sensorStations);
        resolve({ sensorStations: sensorStations });
      })
      .catch((err: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching all sensor stations",
          err
        );
        resolve({ sensorStations: [] });
      });
  });
}

/**
 * The function fetches the newest picture from a sensor station and encodes it as a base64 string.
 * @param {any} event - The event parameter is likely an object representing an event that triggered
 * the function, such as a button click or a page load.
 * @param {any[]} sensorStations - An array of objects representing sensor stations, each with a unique
 * `sensorStationId` property.
 */
function getNewestPicture(event: any, sensorStations: any[]): void {
  for (let sensorStation of sensorStations) {
    sensorStation.newestPicture = new Promise(async (resolve, reject) => {
      await event
        .fetch(
          `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${sensorStation.sensorStationId}`
        )
        .then(async (res: any) => {
          if (!res.ok) {
            // errorHandler(event.locals.user?.personId, "Error while fetching newest picture", res)
            resolve(null);
          }
          let blob = await res.blob();
          let file = new File([blob], "image", { type: blob.type });
          let arrayBuffer = await blob.arrayBuffer();
          let buffer = Buffer.from(arrayBuffer);
          let encodedImage =
            "data:image/" + blob.type + ";base64," + buffer.toString("base64");
          resolve(encodedImage);
        })
        .catch(() => {
          // logger.error("Error while fetching newest picture");
          // toasts.addToast(event.locals.user?.personId, "error", "Error while fetching newest picture");
          resolve(null);
        });
    });
  }
}
