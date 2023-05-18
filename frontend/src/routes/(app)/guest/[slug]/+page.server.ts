import { BACKEND_URL } from "$env/static/private";
import { logger } from "$lib/helper/logger";
import { redirect, error } from "@sveltejs/kit";
import type { Actions } from "./$types";

export async function load(event) {
  const { url, fetch } = event;
  let sensorStationId = url.searchParams.get("sensorStationId");

  let roomName = "";
  let plantName = "";
  let possiblePictures: any[] = [];
  let pictures: any[] = [];

  await fetch(
    `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStationId}`
  ).then(async (res) => {
    if (!res.ok) {
      logger.error("Couldn't find sensorStation");
      throw error(404, { message: "Couldn't find sensorStation" });
    }

    let data = await res.json();
    possiblePictures = data?.pictures;
    roomName = data.roomName;
    plantName = data.plantName;

    for (let possiblePicture of possiblePictures) {
      logger.info("Fetching picture: ", { payload: possiblePicture.pictureId });
      let promise = new Promise(async (resolve, reject) => {
        await fetch(
          `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
        ).then(async (res) => {
          if (!res.ok) {
            logger.error("Couldn't find picture");
            reject(null);
          }

          let blob = await res.blob();
          let file = new File([blob], possiblePicture.pictureId, {
            type: blob.type,
          });
          let arrayBuffer = await blob.arrayBuffer();
          let buffer = Buffer.from(arrayBuffer);
          let encodedImage =
            "data:image/" + file.type + ";base64," + buffer.toString("base64");
          let newPicture: Picture = {
            pictureId: possiblePicture.pictureId,
            imageRef: encodedImage,
            creationDate: new Date(possiblePicture.timeStamp),
          };
          resolve(newPicture);
        });
      });
      let picturePromise = {
        pictureId: possiblePicture.pictureId,
        promise: promise,
      };
      pictures.push(picturePromise);
    }
  });

  return {
    roomName,
    plantName,
    streamed: {
      pictures: pictures,
    },
  };
}

export const actions = {
  default: async ({ request, fetch, url }) => {
    let sensorStationId = url.searchParams.get("sensorStationId");
    let formData = await request.formData();

    try {
      let res = await fetch(
        `${BACKEND_URL}/upload-sensor-station-picture?sensorStationId=${sensorStationId}`,
        {
          method: "POST",
          body: formData,
        }
      );
      res = await res.json();

      return {
        message: "upload complete",
      };
    } catch (e) {}
  },
} satisfies Actions;
