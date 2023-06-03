import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

type Dates = {
  from: Date;
  to: Date;
};

export async function getSensorStationData(
  event: any,
  sensorStation: SensorStation,
  dates: Dates
): Promise<Responses.SensorStationDataResponse> {
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
          errorHandler(
            event.locals.user?.personId,
            "Error while fetching sensor station data",
            data
          );
          reject(data);
          throw error(res.status, "Error while fetching sensor station data");
        }
        let data = await res.json();
        resolve(data);
      })
      .catch((err: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station data",
          err
        );
        throw error(500, "Error while fetching sensor station data");
      });
  });
}
