import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

export async function getSensorStationPictures(
  event: any,
  fetch: any,
  sensorStation: any
) {
  let possiblePictures: any[] = [];
  sensorStation.pictures = [];
  await fetch(
    `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
  )
    .then(async (res: any) => {
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
      possiblePictures = data.pictureIds;
    })
    .catch((e: any) => {
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
    return;
  }
  //---------------------------------------------------------------------
  //---------------------------------------------------------------------
  for (let picture of possiblePictures) {
    let picturePromise = new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-picture?pictureId=${picture.picutreId}`)
        .then(async (res: any) => {
          if (!res.ok) {
            logger.error("Error while fetching picture", { payload: res });
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
            "data:image/" + file.type + ";base64," + buffer.toString("base64");
          let newPicture: Picture = {
            pictureId: picture.pictureId,
            imageRef: encodedImage,
            creationDate: new Date(picture.creationDate),
          };
          resolve(newPicture);
        })
        .catch((e: any) => {
          logger.error("Error while fetching picture", { payload: e });
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
}
