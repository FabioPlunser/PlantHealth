import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This function deletes all pictures associated with a specific sensor station.
 * @param {any} event - The `event` parameter is an object that contains information about the HTTP
 * request that triggered the function. It includes properties such as `request` (an object
 * representing the incoming request), `fetch` (a function for making HTTP requests), and `locals` (an
 * object containing additional information about the request
 */
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
        errorHandler(
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
      errorHandler(
        event.locals.user?.personId,
        "Error while deleting all pictures",
        e
      );
      throw error(500, "Error while deleting picture");
    });
}
