import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This function deletes a sensor station by sending a DELETE request to the backend API.
 * @param {any} event - The `event` parameter is an object that contains information about the HTTP
 * request that triggered the function, including the request object and the fetch function.
 */
export async function deleteSensorStation(event: any) {
  const { request, fetch } = event;
  let formData = await request.formData();
  let sensorStationId: string = String(formData.get("sensorStationId"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);

  await fetch(`${BACKEND_URL}/delete-sensor-station?${params.toString()}`, {
    method: "DELETE",
  })
    .then(async (res: any) => {
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Error while deleting sensor station",
          res
        );
      } else {
        logger.info(`Deleted sensor station = ${sensorStationId}`);
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Deleted sensor station"
        );
      }
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while deleting sensor station",
        e
      );
      throw error(500, "Error while deleting sensor station");
    });
}
