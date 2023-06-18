import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";

/**
 * This function uploads a picture to a sensor station and handles any errors that may occur.
 * @param {any} event - The `event` parameter is an object that contains information about the event
 * that triggered the function. It includes a `request` object that contains information about the
 * incoming request, and a `fetch` function that can be used to make HTTP requests to external APIs.
 */
export async function uploadPicture(event: any) {
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
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while uploading picture",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Picture uploaded"
      );
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while uploading picture",
        e
      );
    });
}
