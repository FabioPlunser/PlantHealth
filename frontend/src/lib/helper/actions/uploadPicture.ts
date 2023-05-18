import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";

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
