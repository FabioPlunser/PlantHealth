import { BACKEND_URL } from "$env/static/private";
import { logger } from "$lib/helper/logger";
import { redirect } from "@sveltejs/kit";
import type { Actions } from "./$types";

export async function load({ locals, params, url, fetch }) {
  let error = "";
  console.log("LOAD GUEST PLANT");
  let roomName = "";
  let plantName = "";
  let possiblePictures = [];
  try {
    let sensorStationId = url.searchParams.get("sensorStationId");
    console.log("LOAD GUEST PLANT 2");
    let res = await fetch(
      `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStationId}`
    );
    console.log("LOAD GUEST PLANT 3");
    if (!res.ok) {
      console.log("LOAD GUEST PLANT 4");
      console.log(await res.json());
      if (res.status === 406) {
        logger.error("Guest couldn't find sensor station", { res });
        throw redirect(303, "/guest");
      }
      error = res.statusText;
      return {
        error,
      };
    }
    console.log("LOAD GUEST PLANT 5");
    res = await res.json();
    possiblePictures = res?.pictures;
    roomName = res.roomName;
    plantName = res.plantName;
  } catch (e) {
    logger.error("Guest couldn't find sensor station", { e });
    error = e.message;
  }

  async function fetchPictures() {
    let pictures = [];
    try {
      for (let possiblePicture of possiblePictures) {
        let res = await fetch(
          `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
        );
        if (!res.ok) throw new Error(res.statusText);
        res = await res.blob();
        let file = new File([res], "image", { type: res.type });
        let arrayBuffer = await res.arrayBuffer();
        let buffer = Buffer.from(arrayBuffer);
        let encodedImage =
          "data:image/" + res.type + ";base64," + buffer.toString("base64");
        let picture = {
          imageRef: "",
          creationDate: new Date(),
          pictureId: "",
        };

        picture.imageRef = encodedImage;
        picture.creationDate = new Date(possiblePicture.timeStamp);
        picture.pictureId = possiblePicture.pictureId;
        pictures.push(picture);
      }
      return pictures;
    } catch (e) {
      error = e.message;
    }
  }

  return {
    error,
    roomName,
    plantName,
    streamed: {
      fetchPictures: fetchPictures(),
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
