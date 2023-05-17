import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { ErrorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

export async function deleteAllPictures(event: any) {
  const { request, fetch } = event;
  let formData = await request.formData();
  let sensorStationId = String(formData.get("sensorStationId"));
  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);

  let requestOptions = {
    method: "POST",
  };

  await fetch(
    `${BACKEND_URL}/delete-all-sensor-station-pictures?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        ErrorHandler(
          event.locals.user?.personId,
          "Error while deleting all pictures",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "All pictures deleted"
      );
    })
    .catch((e: any) => {
      ErrorHandler(
        event.locals.user?.personId,
        "Error while deleting all pictures",
        e
      );
      throw error(500, "Error while deleting picture");
    });
}
