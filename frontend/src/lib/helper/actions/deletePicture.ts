import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This is a TypeScript function that deletes a picture from a sensor station and handles errors.
 * @param {any} event - The `event` parameter is an object that contains information about the event
 * that triggered the function. It includes a `request` object that contains information about the HTTP
 * request that triggered the function, and a `fetch` function that can be used to make HTTP requests
 * to other endpoints.
 */
export async function deletePicture(event: any) {
  const { request, fetch } = event;
  let formData = await request.formData();
  let pictureId = String(formData.get("pictureId"));
  let params = new URLSearchParams();
  params.set("pictureId", pictureId);

  let requestOptions = {
    method: "POST",
  };

  await fetch(
    `${BACKEND_URL}/delete-sensor-station-picture?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while deleting picture",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Picture deleted"
      );
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while deleting picture",
        e
      );
      throw error(500, "Error while deleting picture");
    });
}
