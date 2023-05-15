import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";
import { toasts } from "$stores/toastStore";
import type {
  ResponseSensorValues,
  Sensor,
  SensorStation,
} from "../../../../../app.js";

interface _SensorStation extends SensorStation {
  data: Promise<any>;
  pictures: Promise<any>[];
  [key: string]: any;
}

export async function load({ fetch, depends, cookies }) {
  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());
  // if cookies are set overwrite the dates
  console.log("cookieFrom: ", cookieFrom);
  console.log("cookieTo: ", cookieTo);
  if (cookieFrom !== "" || cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
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

  let data = await res.json();
  let sensorStation: _SensorStation = data.sensorStation;
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
  sensorStation.data = new Promise(async (resolve, reject) => {
    await fetch(
      `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
        sensorStation.sensorStationId
      }&from=${from.toISOString().split(".")[0]}&to=${
        to.toISOString().split(".")[0]
      }`
    )
      .then((response) => {
        logger.info(
          "Get sensor-station-data " +
            "from: " +
            JSON.stringify(from) +
            " to: " +
            JSON.stringify(to)
        );
        if (!response.ok) {
          resolve(null);
        }
        return response.json();
      })
      .then((data) => {
        let responseValues: ResponseSensorValues[] = data.data;
        resolve(responseValues);
      });
  });
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------

  sensorStation.pictures = [];
  for (let possiblePicture of sensorStation.sensorStationPictures) {
    let picturePromise = new Promise<any>(async (resolve, reject) => {
      let pictureResponse = await fetch(
        `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
      );

      if (!pictureResponse.ok) {
        reject(pictureResponse.statusText);
      }

      let blob = await pictureResponse.blob();
      let arrayBuffer = await blob.arrayBuffer();
      let buffer = Buffer.from(arrayBuffer);
      let encodedImage =
        "data:image/" + res.type + ";base64," + buffer.toString("base64");
      let picture: Picture = {
        pictureId: possiblePicture.pictureId,
        imageRef: encodedImage,
        creationDate: new Date(possiblePicture.timeStamp),
      };
      resolve(picture);
    });
    sensorStation.pictures.push(picturePromise);
  }
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
  let gardener = null;
  res = await fetch(`${BACKEND_URL}/get-all-gardener`);
  if (!res.ok) {
    logger.error("Could not get gardener");
    throw error(res.status, "Could not get gardener");
  } else {
    gardener = await res.json();
    gardener = gardener.items;
  }

  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
  logger.info("Got sensor station");
  depends("app:getSensorStation");
  return {
    sensorStation,
    gardener,
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

  update: async ({ request, fetch, locals }) => {
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

    let unassign = Boolean(formData.get("delete"));
    let gardenerId = String(formData.get("gardener"));

    params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId);
    params.set("gardenerId", gardenerId);

    if (unassign) {
      params.set("delete", true.toString());
    }

    let res = await fetch(
      `${BACKEND_URL}/assign-gardener-to-sensor-station?${params.toString()}`,
      {
        method: "POST",
      }
    );
    if (!res.ok) {
      logger.error("Could not assign gardener to sensor station");
      throw error(res.status, "Could not assign gardener to sensor station");
    } else {
      if (unassign) {
        logger.info("Unassigned gardener from sensor station");
        toasts.addToast(
          locals.user.personId,
          "success",
          "Unassigned gardener from sensor station"
        );
      } else {
        logger.info("Assigned gardener to sensor station");
        toasts.addToast(
          locals.user.personId,
          "success",
          "Assigned gardener to sensor station"
        );
      }
    }
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
    let sensorStationId: string = String(formData.get("sensorStationId"));

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId?.toString());

    await fetch(`${BACKEND_URL}/delete-sensor-station?${params.toString()}`, {
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

    if (_from == null || _to == null) {
      return;
    }
    let newFrom = new Date(_from.toString());
    let newTo = new Date(_to.toString());
    console.log(newTo);

    newFrom.setHours(0);
    newFrom.setMinutes(0);
    newFrom.setSeconds(0);

    newTo.setDate(newTo.getDate() + 1);

    logger.info("UpdateFromTo");
    logger.info("from" + JSON.stringify(_from));
    logger.info("to" + JSON.stringify(_to));

    cookies.set("from", newFrom.toISOString(), { path: "/" });
    cookies.set("to", newTo.toISOString(), { path: "/" });
  },

  deletePicture: async ({ request, fetch, locals }) => {
    let formData = await request.formData();
    let pictureId = formData.get("pictureId");

    if (pictureId === null) {
      logger.error("deletePicture: pictureId null");
      toasts.addToast(
        locals.user.personId,
        "error",
        "Failed to delete picture no pictureId"
      );
      return;
    }

    let params = new URLSearchParams();
    params.set("pictureId", pictureId.toString());

    let parametersString = "?" + params.toString();
    await fetch(
      `${BACKEND_URL}/delete-sensor-station-picture${parametersString}`,
      {
        method: "POST",
      }
    ).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to delete picture: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Deleted picture = ${pictureId}`);
        toasts.addToast(locals.user.personId, "success", "Deleted picture");
      }
    });
  },

  deleteAllPictures: async ({ request, fetch, locals }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");

    if (sensorStationId === null) {
      logger.error("deletePicture: pictureId null");
      toasts.addToast(
        locals.user.personId,
        "error",
        "Failed to delete all pictures no sensorStationId"
      );
      return;
    }

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId.toString());

    let parametersString = "?" + params.toString();
    await fetch(
      `${BACKEND_URL}/delete-all-sensor-station-pictures${parametersString}`,
      {
        method: "POST",
      }
    ).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to delete all pictures: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Deleted all pictures = ${sensorStationId}`);
        toasts.addToast(
          locals.user.personId,
          "success",
          "Deleted all pictures"
        );
      }
    });
  },
};
