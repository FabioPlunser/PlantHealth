import { BACKEND_URL } from "$env/static/private";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

type Dates = {
  from: Date;
  to: Date;
};

async function getPictureIds(
  event: any,
  sensorStation: Responses.SensorStationBaseResponse
): Promise<Responses.PlantPicturesResponse> {
  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
  );

  if (!res.ok) {
    errorHandler(
      event.locals.user?.personId,
      "Error while fetching sensor station pictures",
      await res.json()
    );
  }

  return await res.json();
}

export async function getSensorStationPictures(
  event: any,
  sensorStation: Responses.SensorStationBaseResponse
): Promise<SensorStationPicture[]> {
  let pictures = await getPictureIds(event, sensorStation);
  let possiblePictures = pictures.pictures;

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
