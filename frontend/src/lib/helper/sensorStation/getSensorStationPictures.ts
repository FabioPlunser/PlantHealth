import { BACKEND_URL } from "$env/static/private";
import { ErrorHandler } from "./errorHandler";

type Dates = {
  from: Date;
  to: Date;
};

export async function getSensorStationPictures(
  event: any,
  sensorStation: SensorStation
) {
  let possiblePictures = await fetch(
    `${BACKEND_URL}/get-sensor-station-pictures?sensorStationId=${sensorStation.sensorStationId}`
  )
    .then(async (res: any) => {
      if (!res.ok) {
        let data = await res.json();
        ErrorHandler(
          event.locals.user?.personId,
          "Error while fetching sensor station pictures",
          data
        );
      }

      let data = await res.json();
      return data.pictures;
    })
    .catch((err: any) => {
      ErrorHandler(
        event.locals.user?.personId,
        "Error while fetching sensor station pictures",
        err
      );
    });

  let picturePromises: Promise<any>[] = [];

  for (let possiblePicture of possiblePictures) {
    let picturePromise = new Promise(async (resolve, reject) => {
      await fetch(
        `${BACKEND_URL}/get-sensor-station-picturepictureId=${possiblePicture.pictureId}`
      )
        .then(async (res: any) => {
          if (!res.ok) {
            let data = await res.json();
            ErrorHandler(
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
          ErrorHandler(
            event.locals.user?.personId,
            "Error while fetching sensor station picture",
            err
          );
          resolve(null);
        });
    }).catch((err: any) => {
      ErrorHandler(
        event.locals.user?.personId,
        "Error while fetching sensor station picture",
        err
      );
    });
    picturePromises.push(picturePromise);
  }

  return picturePromises;
}
