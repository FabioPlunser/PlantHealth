import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

import { getSensorStationData } from "./getData";
import { getSensorStationPictures } from "./getPictures";

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
  // get all sensor stations available to add to dashboard
  //---------------------------------------------------------------------
  let allSensorStations = new Promise(async (resolve, reject) => {
    await fetch(`${BACKEND_URL}/get-sensor-stations`)
      .then(async (res) => {
        if (!res.ok) {
          logger.error(
            `Error while fetching sensor stations: ${res.status} ${res.statusText}`
          );
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor stations"
          );
          reject(null);
        }
        let data = await res.json();
        let sensorStations = data.sensorStations;
        //---------------------------------------------------------------------
        // get newest picture for possible sensor stations
        //---------------------------------------------------------------------
        for (let foundSensorStation of sensorStations) {
          foundSensorStation.newestPicture = new Promise(
            async (resolve, reject) => {
              await fetch(
                `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${foundSensorStation.sensorStationId}`
              )
                .then(async (pictureResponse) => {
                  if (!pictureResponse.ok) {
                    logger.error(
                      `Error while fetching sensor station picture: ${pictureResponse.status} ${pictureResponse.statusText}`
                    );
                    toasts.addToast(
                      event.locals.user?.personId,
                      "error",
                      "Error while fetching sensor station picture"
                    );
                    resolve(null);
                  }
                  let blob = await pictureResponse.blob();
                  let file = new File([blob], "image", { type: blob.type });
                  let arrayBuffer = await blob.arrayBuffer();
                  let buffer = Buffer.from(arrayBuffer);
                  let encodedImage =
                    "data:image/" +
                    blob.type +
                    ";base64," +
                    buffer.toString("base64");
                  resolve(encodedImage);
                })
                .catch((e) => {
                  logger.error("Error while fetching sensor station picture", {
                    e,
                  });
                  toasts.addToast(
                    event.locals.user?.personId,
                    "error",
                    "Error while fetching sensor station picture"
                  );
                  reject(null);
                });
            }
          );
        }
        resolve(sensorStations);
      })
      .catch((e) => {
        logger.error("Error while fetching sensor stations", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching sensor stations"
        );
        reject(null);
      });
  });
  //---------------------------------------------------------------------
  // get all sensor stations in dashboard or assigned to gardener
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
  //
  //---------------------------------------------------------------------
  for (let sensorStation of dashboard.addedSensorStations) {
    getSensorStationData(event, fetch, sensorStation, from, to);
    getSensorStationPictures(event, fetch, sensorStation);
  }
  //---------------------------------------------------------------------
  // Iterate through all assigned sensor stations and get alle pictures and data as a promise
  //---------------------------------------------------------------------
  for (let sensorStation of dashboard.assignedSensorStations) {
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
          let data = await res.json();
          resolve(data);
        })
        .catch((e) => {
          logger.error("Error while fetching sensor station data", {
            payload: e,
          });
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
          let data = await res.json();
          resolve(data.sensorStation.sensorLimits);
        })
        .catch((e) => {
          logger.error("Error while fetching sensor station data", {
            payload: e,
          });
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
          logger.error("Error while fetching sensor station pictures", {
            payload: res,
          });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor station pictures"
          );
          possiblePictures = [];
        }
        let data = await res.json();
        possiblePictures = data.pictures;
      })
      .catch((e) => {
        logger.error("Error while fetching sensor station pictures", {
          payload: e,
        });
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
      let picturePromise = new Promise(async (resolve, reject) => {
        await fetch(
          `${BACKEND_URL}/get-sensor-station-picture?pictureId=${picture.pictureId}`
        )
          .then(async (res) => {
            if (!res.ok) {
              res = await res.json();
              logger.error("Error while fetching picture", { payload: res });
              toasts.addToast(
                event.locals.user?.personId,
                "error",
                "Error while fetching picture"
              );
              resolve(null);
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
              creationDate: new Date(picture.timeStamp),
            };
            resolve(newPicture);
          })
          .catch((e) => {
            logger.error("Error while fetching picture", { payload: e });
            toasts.addToast(
              event.locals.user?.personId,
              "error",
              "Error while fetching picture"
            );
            reject(null);
          });
      });
      let sensorStationPicture: any = {
        pictureId: picture.pictureId,
        promise: picturePromise,
      };
      sensorStation.pictures.push(sensorStationPicture);
    }
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
  }

  return {
    streamed: {
      sensorStations: allSensorStations,
      dashboard: dashboard,
    },
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
        if (!res.ok) {
          res = await res.json();
          logger.error("Error while updating sensor station", { payload: res });
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
        logger.error("Error while updating sensor station", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while updating sensor station"
        );
        throw error(500, "Error while updating sensor station");
      });
  },

  deletePicture: async (event) => {
    const { request, fetch } = event;
    let formData = await request.formData();
    let pictureId = String(formData.get("pictureId"));
    let params = new URLSearchParams();
    params.set("pictureId", pictureId);

    let requestOptions = {
      method: "POST",
    };

    await fetch(
      `${BACKEND_URL}/delete-sensor-station-picture?${params.toString()}`,
      requestOptions
    )
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          logger.error("Error while deleting picture", { payload: res });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while deleting picture"
          );
          throw error(500, "Error while deleting picture");
        }
        let data = await res.json();
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Picture deleted"
        );
      })
      .catch((e) => {
        logger.error("Error while deleting picture", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while deleting picture"
        );
        throw error(500, "Error while deleting picture");
      });
  },

  deleteAllPictures: async (event) => {
    const { request, fetch } = event;
    let formData = await request.formData();
    let sensorStationId = String(formData.get("sensorStationId"));
    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId);

    let requestOptions = {
      method: "POST",
    };

    await fetch(
      `${BACKEND_URL}/delete-all-sensor-station-pictures?${params.toString()}`,
      requestOptions
    )
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          logger.error("Error while deleting all pictures", { payload: res });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while deleting all pictures"
          );
          throw error(500, "Error while deleting all pictures");
        }
        let data = await res.json();
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "All pictures deleted"
        );
      })
      .catch((e) => {
        logger.error("Error while deleting all pictures", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while deleting all pictures"
        );
        throw error(500, "Error while deleting all pictures");
      });
  },

  uploadPicture: async (event) => {
    const { request, fetch } = event;
    let formData = await request.formData();
    let sensorStationId: string = String(formData.get("sensorStationId"));
    formData.delete("sensorStationId");

    let requestOptions = {
      method: "POST",
      body: formData,
    };
    await fetch(
      `${BACKEND_URL}/upload-sensor-station-picture?sensorStationId=${sensorStationId}`,
      requestOptions
    )
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          logger.error("Error while uploading picture", { payload: res });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while uploading picture"
          );
          throw error(500, "Error while uploading picture");
        }
        let data = await res.json();
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Picture uploaded"
        );
      })
      .catch((e) => {
        logger.error("Error while uploading picture", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while uploading picture"
        );
        throw error(500, "Error while uploading picture");
      });
  },

  updateLimit: async (event) => {
    const { request, fetch } = event;
    const formData = await request.formData();
    const zodData = limitsSchema.safeParse({
      upperLimit: Number(formData.get("upperLimit")),
      lowerLimit: Number(formData.get("lowerLimit")),
      thresholdDuration: Number(formData.get("thresholdDuration")),
    });

    let sensorStationId: string = String(formData.get("sensorStationId"));
    let sensor: string = String(formData.get("sensor"));

    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { id: sensor, error: true, errors });
    }

    let upperLimit: number = Number(formData.get("upperLimit"));
    let lowerLimit: number = Number(formData.get("lowerLimit"));
    let thresholdDuration: number = Number(formData.get("thresholdDuration"));

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId);

    let requestOptions = {
      method: "POST",
      body: JSON.stringify([
        {
          upperLimit,
          lowerLimit,
          thresholdDuration,
          sensor: {
            type: sensor,
          },
        },
      ]),
      headers: {
        "Content-Type": "application/json",
      },
    };

    await fetch(
      `${BACKEND_URL}/update-sensor-station?${params.toString()}`,
      requestOptions
    )
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          logger.error("Error while updating limit", { payload: res });
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while updating limit"
          );
          throw error(500, "Error while updating limit");
        }
        let data = await res.json();
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Limit updated"
        );
      })
      .catch((e) => {
        logger.error("Error while updating limit", { payload: e });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while updating limit"
        );
        throw error(500, "Error while updating limit");
      });
  },

  updateFromTo: async (event) => {
    let fromData = await event.request.formData();
    let _from = String(fromData.get("from"));
    let _to = String(fromData.get("to"));

    let from = new Date(_from);
    let to = new Date(_to);

    from.setHours(0, 0, 0, 0);
    to.setHours(23, 59, 59, 999);

    logger.info("Update from to", { payload: { from, to } });

    event.cookies.set("from", from.toISOString(), { path: "/" });
    event.cookies.set("to", to.toISOString(), { path: "/" });
  },

  addToDashboard: async (event) => {
    const { request, fetch } = event;
    let formdData = await request.formData();
    let sensorStationId: string = String(formdData.get("sensorStationId"));

    await fetch(
      `${BACKEND_URL}/add-to-dashboard?sensorStationId=${sensorStationId}`,
      { method: "POST" }
    ).then(async (res) => {
      if (!res.ok) {
        res = await res.json();
        logger.error("Error while adding to dashboard", { payload: res });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while adding to dashboard"
        );
        throw error(500, "Error while adding to dashboard");
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Added to dashboard"
      );
    });
  },

  removeFromDashboard: async (event) => {
    const { request, fetch } = event;
    let formdData = await request.formData();
    let sensorStationId: string = String(formdData.get("sensorStationId"));

    await fetch(
      `${BACKEND_URL}/remove-from-dashboard?sensorStationId=${sensorStationId}`,
      { method: "POST" }
    ).then(async (res) => {
      if (!res.ok) {
        res = await res.json();
        logger.error("Error while removing from dashboard", { payload: res });
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while removing from dashboard"
        );
        throw error(500, "Error while removing from dashboard");
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Removed from dashboard"
      );
    });
  },
};
