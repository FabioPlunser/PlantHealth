import { BACKEND_URL } from "$env/static/private";
import { ErrorHandler } from "../errorHandler";

export async function getAllSensorStations(event: any): Promise<any> {
  return new Promise(async (resolve, reject) => {
    await event
      .fetch(`${BACKEND_URL}/get-sensor-stations`)
      .then(async (res: any) => {
        if (!res.ok) {
          let data = await res.json();
          ErrorHandler(
            event.locals.user?.personId,
            "Error while fetching all sensor stations",
            data
          );
          resolve([]);
        }

        let data = await res.json();
        let sensorStations = data?.sensorStations;
        if (sensorStations.lenght === 0) return [];
        //---------------------------------------------------------------------
        // get newest picture of sensor stations
        //---------------------------------------------------------------------
        getNewestPicture(event, sensorStations);
        resolve(sensorStations);
      })
      .catch((err: any) => {
        ErrorHandler(
          event.locals.user?.personId,
          "Error while fetching all sensor stations",
          err
        );
        resolve([]);
      });
  });
}

function getNewestPicture(event: any, sensorStations: any[]): void {
  for (let sensorStation of sensorStations) {
    sensorStation.newestPicture = new Promise(async (resolve, reject) => {
      await event
        .fetch(
          `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${sensorStation.sensorStationId}`
        )
        .then(async (res: any) => {
          if (!res.ok) {
            // ErrorHandler(event.locals.user?.personId, "Error while fetching newest picture", res)
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
