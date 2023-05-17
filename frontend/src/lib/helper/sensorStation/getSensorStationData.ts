import { BACKEND_URL } from "$env/static/private";
import { ErrorHandler } from "./errorHandler";

type Dates = {
  from: Date;
  to: Date;
};

export async function getSensorStationData(
  event: any,
  sensorStation: SensorStation,
  dates: Dates
) {
  return await new Promise(async (resolve, reject) => {
    await event
      .fetch(
        `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
          sensorStation.sensorStationId
        }&from=${dates.from.toISOString().split(".")[0]}&to=${
          dates.to.toISOString().split(".")[0]
        }`
      )
      .then(async (res: any) => {
        if (!res.ok) {
          let data = await res.json();
          ErrorHandler(
            event.locals.user?.personId,
            "Error while fetching sensor station data",
            data
          );
          reject(data);
        }
        let data = await res.json();
        resolve(data);
      })
      .catch((err: any) => {
        ErrorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station data",
          err
        );
        reject(err);
      });
  }).catch((err: any) => {
    ErrorHandler(
      event.locals.user?.personId,
      "Error while fetching sensor station data",
      err
    );
  });
}
