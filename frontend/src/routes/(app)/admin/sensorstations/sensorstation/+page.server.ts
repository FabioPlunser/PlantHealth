import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";
import { toasts } from "$stores/toastStore";
import { apSensorStations } from "../../../../../lib/stores/apSensorStations";

export async function load({ fetch, depends, cookies }) {
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
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------

  let sensorStationId = cookies.get("sensorStationId");

  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  if (!res.ok) {
    logger.error("Could not get sensor station");
    throw error(res.status, "Could not get sensor station");
  }

  let sensorStation = await res.json();
  sensorStation = sensorStation.sensorStation;
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
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
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
  sensorStation.pictures = [];
  for (let possiblePicture of sensorStation.sensorStationPictures) {
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
      sensorStation.pictures.push(picture);
    });
  }
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------

  // res = await res.json();
  logger.info("Got sensor station");
  depends("app:getSensorStation");
  return {
    sensorStation,
    dates: {
      from,
      to,
    },
  };
}

const nameSchema = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .min(6, { message: "Name must be at least 6 characters" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim(),
});

const limitsSchema = z.object({
  upperLimit: z
    .number({ required_error: "Upper Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  lowerLimit: z
    .number({ required_error: "Lower Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  thresholdDuration: z
    .number({ required_error: "Threshold Duration is required" })
    .positive({ message: "Duration has to be positive" }),
});

export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ request, fetch }) => {
    const formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let unlocked = formData.get("unlocked");

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId?.toString() ?? "");
    params.set("unlocked", unlocked?.toString() ?? "");

    let parametersString = "?" + params.toString();
    await fetch(
      `${BACKEND_URL}/set-unlocked-sensor-station${parametersString}`,
      {
        method: "POST",
      }
    ).then((response) => {
      let time = new Date().toLocaleString();
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        throw error(response.status, response.statusText);
      } else {
        logger.info(
          `unlocked set to = ${unlocked} for sensor station with id = ${sensorStationId}`
        );
      }
    });
  },

  updateName: async ({ request, fetch, locals }) => {
    const formData = await request.formData();
    const zodData = nameSchema.safeParse(Object.fromEntries(formData));

    // validate name input
    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }

    let sensorStationId: string = String(formData.get("sensorStationId"));
    let sensorStationName: string = String(formData.get("name"));

    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId);
    params.set("sensorStationName", sensorStationName);

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/updata-sensor-station${parametersString}`, {
      method: "POST",
      body: JSON.stringify({
        limits: [],
      }),
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to update name: ${response.status} ${response.message}`
        );
      } else {
        logger.info(
          `updated sensor station name for sensor station with id = ${sensorStationId} to name = ${sensorStationName}`
        );
        toasts.addToast(
          locals.user.personId,
          "success",
          "Updated sensor station name"
        );
      }
    });
  },

  updateLimit: async ({ request, fetch, locals }) => {
    const formData = await request.formData();
    const zodData = limitsSchema.safeParse(Object.fromEntries(formData));

    // validate limit input
    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }

    let sensorStationId: string = String(formData.get("sensorStationId"));
    let sensor: Sensor | null = formData.get("sensor");
    let upperLimit: number = parseFloat(String(formData.get("upperLimit")));
    let lowerLimit: number = parseFloat(String(formData.get("lowerLimit")));
    let thresholdDuration: number = parseFloat(
      String(formData.get("thresholdDuration"))
    );

    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId);

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/updata-sensor-station${parametersString}`, {
      method: "POST",
      body: JSON.stringify({
        limits: [
          {
            upperLimit,
            lowerLimit,
            thresholdDuration,
            sensor,
          },
        ],
      }),
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to update limits: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Limits updated for ${sensor}`);
        toasts.addToast(locals.user.personId, "success", "Updated limits");
      }
    });
  },

  delete: async ({ request, fetch, locals }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId?.toString() ?? "");

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/delete-sensor-station${parametersString}`, {
      method: "DELETE",
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to delete sensor station: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Deleted sensor station = ${sensorStationId}`);
        toasts.addToast(
          locals.user.personId,
          "success",
          "Deleted sensor station"
        );
      }
    });
  },
  updateFromTo: async ({ request, fetch, url, cookies, locals }) => {
    let formData = await request.formData();
    let _from = formData.get("from");
    let _to = formData.get("to");

    _from = new Date(_from);
    _to = new Date(_to);
    _from.setHours(0);
    _from.setMinutes(0);
    _from.setSeconds(0);

    _to.setHours(23);
    _to.setMinutes(59);
    _to.setSeconds(59);

    logger.info("UpdateFromTo");
    logger.info("from" + JSON.stringify(_from));
    logger.info("to" + JSON.stringify(_to));

    cookies.set("from", _from, { path: "/" });
    cookies.set("to", _to, { path: "/" });
  },
};
