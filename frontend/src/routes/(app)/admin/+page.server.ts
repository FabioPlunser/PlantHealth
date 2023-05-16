import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

/**
 * Load function to fetch admint dashboard
 * @param event
 * @returns
 */
export async function load(event) {
  const { cookies, fetch } = event;

  //---------------------------------------------------------------------
  /**
   * Check if any date is set if not set default to last 7 days
   */
  //---------------------------------------------------------------------
  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());

  if (cookieFrom !== "" && cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }
  //---------------------------------------------------------------------
  /**
   * Get all available sensor stations to be added to the dashboard
   */
  //---------------------------------------------------------------------

  let allSensorStations = new Promise(async (resolve, reject) => {
    await fetch(`${BACKEND_URL}/get-sensor-stations`)
      .then(async (res) => {
        if (!res.ok) {
          logger.error("Error while fetching all sensor stations");
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching sensor stations"
          );
          reject(await res.json());
        }
        let data = await res.json();
        let sensorStations = data?.sensorStations;
        //---------------------------------------------------------------------
        // get newest picture of sensor stations
        //---------------------------------------------------------------------
        if (sensorStations.length === 0) resolve([]);
        for (let sensorStation of sensorStations) {
          console.log(sensorStation);
          sensorStation.newestPicture = new Promise(async (resolve, reject) => {
            await fetch(
              `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${sensorStation.sensorStationId}`
            )
              .then(async (res) => {
                if (!res.ok) {
                  // logger.error("Error while fetching newest picture");
                  // toasts.addToast(event.locals.user?.personId, "error", "Error while fetching newest picture");
                  resolve(null);
                }
                let blob = await res.blob();
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
              .catch((err) => {
                // logger.error("Error while fetching newest picture");
                // toasts.addToast(event.locals.user?.personId, "error", "Error while fetching newest picture");
                resolve(null);
              });
          });
        }
        resolve(sensorStations);
      })
      .catch((err) => {
        logger.error("Error while fetching all sensor stations");
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching sensor stations"
        );
        reject(err);
      });
  });
  //---------------------------------------------------------------------
  // get all sensor stations in dashboard
  //---------------------------------------------------------------------
  let numbers = await fetch(`${BACKEND_URL}/get-dashboard`)
    .then(async (res) => {
      if (!res.ok) {
        logger.error("Error while fetching dashboard sensor stations");
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching dashboard sensor stations"
        );
        return await res.json();
      }
      let data = await res.json();
      return {
        users: data?.numOfUsers,
        accessPoints: data?.numOfConnectedAccessPoints,
        sensorStations: data?.numOfConnectedSensorStations,
      };
    })
    .catch((err) => {
      logger.error("Error while fetching dashboard sensor stations");
      toasts.addToast(
        event.locals.user?.personId,
        "error",
        "Error while fetching dashboard sensor stations"
      );
    });

  let dashboardSensorStations = new Promise<any[]>(async (resolve, reject) => {
    await fetch(`${BACKEND_URL}/get-dashboard`)
      .then(async (res) => {
        if (!res.ok) {
          logger.error("Error while fetching dashboard sensor stations");
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while fetching dashboard sensor stations"
          );
          reject(await res.json());
        }
        let data = await res.json();
        let sensorStations = data?.sensorStations;
        if (sensorStations.length === 0) resolve([]);
        //---------------------------------------------------------------------
        // get all pictures of all sensor station, get data of all sensor stations
        //---------------------------------------------------------------------
        for (let sensorStation of sensorStations) {
          sensorStation.data = new Promise(async (resovle, reject) => {
            await fetch(
              `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
                sensorStation.sensorStationId
              }&from=${from.toISOString().split(".")[0]}&to=${
                to.toISOString().split(".")[0]
              }`
            )
              .then(async (res) => {
                if (!res.ok) {
                  logger.error("Error while fetching sensor station data");
                  toasts.addToast(
                    event.locals.user?.personId,
                    "error",
                    "Error while fetching sensor station data"
                  );
                  reject(await res.json());
                }
                let data = await res.json();
                console.log(data);
                resovle(data);
              })
              .catch((err) => {
                logger.error("Error while fetching sensor station data");
                toasts.addToast(
                  event.locals.user?.personId,
                  "error",
                  "Error while fetching sensor station data"
                );
                reject(err);
              });
          }).catch((err) => {
            logger.error("Error while fetching sensor station data");
            toasts.addToast(
              event.locals.user?.personId,
              "error",
              "Error while fetching sensor station data"
            );
          });

          let possiblePictures = await fetch(
            `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
          )
            .then(async (res) => {
              if (!res.ok) {
                logger.error("Error while fetching sensor station pictures");
                toasts.addToast(
                  event.locals.user?.personId,
                  "error",
                  "Error while fetching sensor station pictures"
                );
                return [];
              }
              let data = await res.json();
              console.log(data);
              return data.pictures;
            })
            .catch((err) => {
              logger.error("Error while fetching sensor station pictures");
              toasts.addToast(
                event.locals.user?.personId,
                "error",
                "Error while fetching sensor station pictures"
              );
              return [];
            });

          if (possiblePictures.length === 0) {
            sensorStation.pictures = [];
            continue;
          }
          sensorStation.pictures = [];
          for (let possiblePicture of possiblePictures) {
            let picturePromise = new Promise<Picture | null>(
              async (resolve, reject) => {
                await fetch(
                  `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
                )
                  .then(async (res) => {
                    if (!res.ok) {
                      logger.error(
                        "Error while fetching sensor station picture"
                      );
                      toasts.addToast(
                        event.locals.user?.personId,
                        "error",
                        "Error while fetching sensor station picture"
                      );
                      resolve(null);
                    }
                    let blob = await res.blob();
                    let file = new File([blob], "image", { type: blob.type });
                    let arrayBuffer = await blob.arrayBuffer();
                    let buffer = Buffer.from(arrayBuffer);
                    let encodedImage =
                      "data:image/" +
                      blob.type +
                      ";base64," +
                      buffer.toString("base64");

                    let newPicture: Picture = {
                      pictureId: possiblePicture.pictureId,
                      imageRef: encodedImage,
                      creationDate: new Date(possiblePicture.timeStamp),
                    };
                    resolve(newPicture);
                  })
                  .catch((err) => {
                    logger.error("Error while fetching sensor station picture");
                    toasts.addToast(
                      event.locals.user?.personId,
                      "error",
                      "Error while fetching sensor station picture"
                    );
                    resolve(null);
                  });
              }
            ).catch((err) => {
              logger.error("Error while fetching sensor station picture");
              toasts.addToast(
                event.locals.user?.personId,
                "error",
                "Error while fetching sensor station picture"
              );
            });
            sensorStation.pictures.push(picturePromise);
          }
        }

        resolve(sensorStations);
      })
      .catch((err) => {
        logger.error("Error while fetching dashboard sensor stations");
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while fetching dashboard sensor stations"
        );
        reject(err);
      });
  });

  return {
    dates: {
      from,
      to,
    },
    numbers,
    streamed: {
      allSensorStations,
      dashboardSensorStations,
    },
  };
}

export const actions = {
  addToDashboard: async (event) => {
    const { request, fetch } = event;
    let formData = await request.formData();
    let sensorStationId: string = String(formData.get("sensorStationId"));

    await fetch(
      `${BACKEND_URL}/add-to-dashboard?sensorStationId=${sensorStationId}`,
      { method: "POST" }
    )
      .then(async (res: any) => {
        if (!res.ok) {
          logger.error("Error while adding sensor station to dashboard");
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while adding sensor station to dashboard"
          );
        }
      })
      .catch((err) => {
        logger.error("Error while adding sensor station to dashboard");
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while adding sensor station to dashboard"
        );
      });
  },

  removeFromDashboard: async (event) => {
    const { request, fetch } = event;
    let formData = await request.formData();
    let sensorStationId: string = String(formData.get("sensorStationId"));

    await fetch(
      `${BACKEND_URL}/remove-from-dashboard?sensorStationId=${sensorStationId}`,
      { method: "POST" }
    )
      .then(async (res: any) => {
        if (!res.ok) {
          logger.error("Error while removing sensor station from dashboard");
          toasts.addToast(
            event.locals.user?.personId,
            "error",
            "Error while removing sensor station from dashboard"
          );
        }
      })
      .catch((err) => {
        logger.error("Error while removing sensor station from dashboard");
        toasts.addToast(
          event.locals.user?.personId,
          "error",
          "Error while removing sensor station from dashboard"
        );
      });
  },
};
