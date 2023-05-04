import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";

export async function load({ locals, params, url, fetch }) {
  let error = "";

  let roomName = "";
  let plantName = "";
  let possiblePictures = [];
  try {
    let sensorStationId = url.searchParams.get("sensorStationId");
    let res = await fetch(
      `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStationId}`
    );
    if (!res.ok) {
      error = res.statusText;
      return {
        error,
      };
    }
    res = await res.json();
    possiblePictures = res?.pictures;
    roomName = res.roomName;
    plantName = res.plantName;
  } catch (e) {
    console.log(e);
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
        let picture: Picture = {
          imageRef: "",
          creationDate: new Date(),
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
