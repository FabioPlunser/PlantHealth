import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

type Dates = {
  from: Date;
  to: Date;
};

/**
 * This is an asynchronous function that fetches sensor station data from a backend API using a sensor
 * station ID and date range.
 * @param {any} event - The event parameter is an object that represents the event that triggered the
 * function. It is likely an HTTP request object.
 * @param {SensorStation} sensorStation - The sensorStation parameter is an object that contains
 * information about a specific sensor station, including its sensorStationId.
 * @param {Dates} dates - The `dates` parameter is an object that contains two properties: `from` and
 * `to`. Both properties are `Date` objects that represent the start and end dates of the time range
 * for which sensor station data is being requested.
 * @returns a Promise that resolves to the data fetched from a backend API endpoint for a specific
 * sensor station and date range. If there is an error during the fetch or Promise rejection, an error
 * message is thrown.
 */
export async function getSensorStationData(
  event: any,
  sensorStation: Responses.SensorStationBaseResponse,
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
