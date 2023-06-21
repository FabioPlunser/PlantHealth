import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

type Dates = {
  from: Date;
  to: Date;
};

/**
 * This function fetches pictures for a given sensor station and returns an array of promises that
 * resolve to the actual pictures.
 * @param {any} event - The event parameter is an object that contains information about the event that
 * triggered the function. It is likely used to log errors and track user activity.
 * @param {SensorStation} sensorStation - The `sensorStation` parameter is an object that represents a
 * sensor station. It contains a `sensorStationId` property that is used to fetch pictures associated
 * with that sensor station.
 * @returns An array of objects containing the picture ID and a promise that resolves to a Picture
 * object.
 */
export async function getSensorStationPictures(
  event: any,
  sensorStation: Responses.SensorStationBaseResponse
) {
  let possiblePictures = await fetch(
    `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
  )
    .then(async (res: any) => {
      if (!res.ok) {
        let data = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station pictures",
          data
        );
        throw error(res.status, "Error while fetching sensor station pictures");
      }

      let data = await res.json();
      return data.pictures;
    })
    .catch((err: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while fetching sensor station pictures",
        err
      );
      throw error(500, "Error while fetching sensor station pictures");
    });

  let picturePromises: any[] = [];

  for (let possiblePicture of possiblePictures) {
    let picturePromise = new Promise(async (resolve, reject) => {
      await fetch(
        `${BACKEND_URL}/get-sensor-station-picture?pictureId=${possiblePicture.pictureId}`
      )
        .then(async (res: any) => {
          if (!res.ok) {
            let data = await res.json();
            errorHandler(
              event.locals.user?.personId,
              "Error while fetching sensor station picture",
              data
            );
            resolve(null);
          }
          let blob = await res.blob();
          let file = new File([blob], "image", { type: blob.type });
          let arrayBuffer = await blob.arrayBuffer();
          let buffer = Buffer.from(arrayBuffer);
          let encodedImage =
            "data:image/" + blob.type + ";base64," + buffer.toString("base64");

          let newPicture: Picture = {
            pictureId: possiblePicture.pictureId,
            imageRef: encodedImage,
            creationDate: new Date(possiblePicture.timeStamp),
          };
          resolve(newPicture);
        })
        .catch((err: any) => {
          errorHandler(
            event.locals.user?.personId,
            "Error while fetching sensor station picture",
            err
          );
          resolve(null);
        });
    });

    let picture = {
      pictureId: possiblePicture.pictureId,
      promise: picturePromise,
    };
    picturePromises.push(picture);
  }
  return picturePromises;
}
