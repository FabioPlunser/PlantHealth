import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";

interface Dashboard {
  sensorStations: [
    {
      data: Promise;
      name: string;
      [key: string]: any;
    }
  ];
}

/**
 * This function loads data from a backend API and returns a dashboard object, dates, and sensor
 * station information.
 * @param  - - `locals`: an object containing local variables that can be used in the function
 * @returns An object with properties `dashboard`, `dates`, and `sensorStations`.
 */
export async function load({ locals, fetch, cookies }) {
  // get dates from cookies
  let from = cookies.get("from");
  let to = cookies.get("to");

  // if no dates are set, set them to the last 7 days
  if (!from || !to) {
    from = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    to = new Date(Date.now());
    cookies.set("from", from, { path: "/" });
    cookies.set("to", to, { path: "/" });
    logger.info("No dates set, setting to last 7 days", { from, to });
  } else {
    from = new Date(from);
    to = new Date(to);
  }

  // get all possible sensor stations from backend
  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  res = await res.json();
  logger.info("get-sensor-stations", { res });

  // get newest picture for each sensor station
  let sensorStations = res?.sensorStations;
  for (let foundSensorStation of res?.sensorStations) {
    logger.info("get-newest-sensor-station-picture", { foundSensorStation });
    foundSensorStation.newestPicture = new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${foundSensorStation.sensorStationId}`
      );
      if (res.status !== 200) {
        foundSensorStation.picture = null;
        resolve(null);
      } else {
        res = await res.blob();
        let file = new File([res], "image", { type: res.type });
        let arrayBuffer = await res.arrayBuffer();
        let buffer = Buffer.from(arrayBuffer);
        let encodedImage =
          "data:image/" + res.type + ";base64," + buffer.toString("base64");
        resolve(encodedImage);
      }
    });
    sensorStations[sensorStations.indexOf(foundSensorStation)] =
      foundSensorStation;
  }

  // fetch dashboard data
  let dashboard: Dashboard = await fetch(`${BACKEND_URL}/get-dashboard`);
  dashboard = await dashboard.json();
  logger.info("get-dashboard", { dashboard });

  for (let sensorStation of dashboard.sensorStations) {
    logger.info("get-sensor-station-data", { sensorStation });
    sensorStation.data = new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
          sensorStation.sensorStationId
        }&from=${from.toISOString().split(".")[0]}&to=${
          to.toISOString().split(".")[0]
        }`
      );
      if (res.status != 200) {
        resolve(null);
      }
      res = await res.json();
      resolve(res);
    });

    let res = await fetch(
      `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
    );
    res = await res.json();
    logger.info("get-sensor-station-pictures", { res });

    sensorStation.pictures = [];
    for (let possiblePicture of res.pictures) {
      logger.info("get-sensor-station-picture", { possiblePicture });
      let picturePromise = new Promise(async (resolve, reject) => {
        let res = await fetch(
          `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
        );
        if (!res.ok) {
          reject(res.statusText);
          throw new Error(res.statusText);
        }
        res = await res.blob();
        let file = new File([res], "image", { type: res.type });
        let arrayBuffer = await res.arrayBuffer();
        let buffer = Buffer.from(arrayBuffer);
        let encodedImage =
          "data:image/" + res.type + ";base64," + buffer.toString("base64");
        let picture: Picture = {
          imageRef: "",
          creationDate: new Date(),
        };
        picture.imageRef = encodedImage;
        picture.creationDate = new Date(possiblePicture.timeStamp);
        picture.pictureId = possiblePicture.pictureId;
        resolve(picture);
      });

      sensorStation.pictures.push(picturePromise);
    }
  }

  return {
    dashboard,
    dates: {
      from,
      to,
    },
    sensorStations: sensorStations,
  };
}

export const actions = {
  /* This is an action function that adds a sensor station to the user's dashboard. It receives an HTTP
  request object, and uses it to extract the form data submitted by the user, which includes the ID
  of the sensor station to be added. It then sends a POST request to the backend API endpoint
  `/add-to-dashboard` with the sensor station ID as a query parameter. The function is asynchronous,
  as it uses the `await` keyword to wait for the response from the API before continuing. */
  addToDashboard: async ({ request, fetch, url }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");

    let res = await fetch(
      `${BACKEND_URL}/add-to-dashboard?sensorStationId=${sensorStationId}`,
      {
        method: "POST",
      }
    );
    res = await res.json();
    logger.info("addToDashboard", { res });
  },
  /* `removeFromDashboard` is an action function that removes a sensor station from the user's
  dashboard. It receives an HTTP request object, extracts the form data submitted by the user, which
  includes the ID of the sensor station to be removed. It then sends a DELETE request to the backend
  API endpoint `/remove-from-dashboard` with the sensor station ID as a query parameter. The
  function is asynchronous, as it uses the `await` keyword to wait for the response from the API
  before continuing. It also logs the response from the API to the console. */
  removeFromDashboard: async ({ request, fetch, url }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");

    let res = await fetch(
      `${BACKEND_URL}/remove-from-dashboard?sensorStationId=${sensorStationId}`,
      {
        method: "DELETE",
      }
    );
    res = await res.json();
    logger.info("removeFromDashboard", { res });
  },

  /* `updateFromTo` is an action function that is called when the user updates the date range for the
  dashboard. It receives an HTTP request object, extracts the form data submitted by the user, which
  includes the new `from` and `to` dates. It then converts these dates to `Date` objects and logs
  them to the console using the `logger` helper function. Finally, it sets the `from` and `to`
  cookies with the new dates, so that they can be used in subsequent requests. */
  updateFromTo: async ({ request, fetch, url, cookies }) => {
    let formData = await request.formData();
    let _from = formData.get("from");
    let _to = formData.get("to");

    _from = new Date(_from);
    _to = new Date(_to);

    logger.info("UpdateFromTo");
    logger.info("from", { _from });
    logger.info("to", { _to });

    cookies.set("from", _from, { path: "/" });
    cookies.set("to", _to, { path: "/" });
  },
} satisfies Actions;
