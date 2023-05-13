import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

interface Dashboard {
  sensorStations: SensorStation[];
}

export async function load(event) {
  const { cookies, fetch } = event;

  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());

  if (cookieFrom !== "" && cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }
  //---------------------------------------------------------------------
  //---------------------------------------------------------------------
  let res = await fetch(`${BACKEND_URL}/get-dashboard`);
  if (!res.ok) {
    logger.error(
      `Error while fetching dashboard data: ${res.status} ${res.statusText}`
    );
    throw error(res.status, "Error while fetching dashboard data");
  }
  let dashboard = await res.json();

  //---------------------------------------------------------------------
  // Iterate through all sensor stations and get alle pictures and data as a promise
  //---------------------------------------------------------------------
  console.log(dashboard);
  for (let sensorStation of dashboard.sensorStations) {
    console.log("sensorStation", sensorStation);
    //---------------------------------------------------------------------
    // Get sensor station data
    //---------------------------------------------------------------------
    sensorStation.data = new Promise(async (resolve, reject) => {
      await fetch(
        `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
          sensorStation.sensorStationId
        }&from=${from.toISOString().split(".")[0]}&to=${
          to.toISOString().split(".")[0]
        }`
      )
        .then(async (res) => {
          console.log("Get-sensor-station-data", res);
          let data = await res.json();
          console.log("Get-sensor-station-data", data);
          resolve(data);
        })
        .catch((e) => {
          logger.error("Error while fetching sensor station data", { e });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor station data"
          );
          reject(e);
        });
    });
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    sensorStation.limits = new Promise(async (resolve, reject) => {
      await fetch(
        `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStation.sensorStationId}`
      )
        .then(async (res) => {
          console.log("Get-sensor-station", res);
          let data = await res.json();
          console.log("Get-sensor-station", data);
          resolve(data.sensorStation.sensorLimits);
        })
        .catch((e) => {
          logger.error("Error while fetching sensor station data", { e });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor station data"
          );
          reject(e);
        });
    });
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    let possiblePictures: any[] = [];
    sensorStation.pictures = [];
    await fetch(
      `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
    )
      .then(async (res) => {
        if (!res.ok) {
          logger.error("Error while fetching sensor station pictures", { res });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor station pictures"
          );
          possiblePictures = [];
        }
        let data = await res.json();
        console.log("SensorStationPicture", data);
        possiblePictures = data.pictureIds;
      })
      .catch((e) => {
        logger.error("Error while fetching sensor station pictures", { e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching sensor station pictures"
        );
        possiblePictures = [];
        throw error(500, "Error while fetching sensor station pictures");
      });
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    if (!possiblePictures) {
      sensorStation.pictures = [];
      continue;
    }
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    for (let picture of possiblePictures) {
      console.log("Picture", picture);
      let picturePromise = new Promise(async (resolve, reject) => {
        await fetch(`${BACKEND_URL}/get-picture?pictureId=${picture.picutreId}`)
          .then(async (res) => {
            if (!res.ok) {
              logger.error("Error while fetching picture", { res });
              toasts.addToast(
                event.locals.user?.personId,
                "error",
                "Error while fetching picture"
              );
              reject(null);
            }
            let blob = await res.blob();
            let file = new File([blob], picture.pictureId, { type: blob.type });
            let arrayBuffer = await blob.arrayBuffer();
            let buffer = Buffer.from(arrayBuffer);
            let encodedImage =
              "data:image/" +
              file.type +
              ";base64," +
              buffer.toString("base64");
            let newPicture: Picture = {
              pictureId: picture.pictureId,
              imageRef: encodedImage,
              creationDate: new Date(picture.creationDate),
            };
            console.log(newPicture);
            resolve(newPicture);
          })
          .catch((e) => {
            logger.error("Error while fetching picture", { e });
            toasts.addToast(
              event.locals.user?.personId,
              "error",
              "Error while fetching picture"
            );
            reject(null);
          });
      });
      sensorStation.pictures.push(picturePromise);
    }
    console.log("hure");
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
  }

  return {
    dashboard,
    dates: {
      from,
      to,
    },
  };
}

//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------

const update = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim(),
  transferInterval: z
    .number({ required_error: "Transfer interval is required" })
    .positive({ message: "Transfer interval has to be positive" }),
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
  updateSensorStation: async (event) => {
    const { request, fetch } = event;
    const formData = await request.formData();
    console.log(formData);
    const zodData = update.safeParse({
      name: formData.get("name"),
      transferInterval: Number(formData.get("transferInterval")),
    });

    let sensorStationId: string = String(formData.get("sensorStationId"));

    // validate name input
    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { id: sensorStationId, error: true, errors });
    }

    let sensorStationName: string = String(formData.get("name"));
    let transferInterval: number = Number(formData.get("transferInterval"));

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId);
    params.set("sensorStationName", sensorStationName);
    params.set("transferInterval", JSON.stringify(transferInterval));

    let requestOptions = {
      method: "POST",
      body: JSON.stringify([]),
      headers: {
        "Content-Type": "application/json",
      },
    };
    await fetch(
      `${BACKEND_URL}/update-sensor-station?${params.toString()}`,
      requestOptions
    )
      .then(async (res) => {
        console.log(res);
        if (!res.ok) {
          res = await res.json();
          console.log(res);
          logger.error("Error while updating sensor station" + String(res));
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while updating sensor station"
          );
          throw error(500, "Error while updating sensor station");
        }
        let data = await res.json();
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Sensor station updated"
        );
      })
      .catch((e) => {
        logger.error("Error while updating sensor station", { e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while updating sensor station"
        );
        throw error(500, "Error while updating sensor station");
      });
  },

  deletePicture: async (e) => {},
};
