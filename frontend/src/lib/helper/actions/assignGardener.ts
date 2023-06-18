import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

/**
 * This function assigns a gardener to a sensor station or unassigns them if specified, by making a
 * POST request to a backend API endpoint.
 * @param {any} event - The event parameter is an object that contains information about the HTTP
 * request that triggered the function, such as the request method, headers, and body. It also contains
 * a fetch function that can be used to make HTTP requests to other APIs.
 * @param {any} [formData] - formData is an optional parameter that represents the data submitted in a
 * form. If it is not provided, the function will use the request object to retrieve the form data.
 */
export async function assignGardener(event: any, formData?: any) {
  const { request, fetch } = event;
  if (!formData) {
    formData = await request.formData();
  }

  let sensorStationId = String(formData.get("sensorStationId"));
  let gardenerId = String(formData.get("gardener"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);
  params.set("gardenerId", gardenerId);

  if (gardenerId === "") {
    params.set("delete", true.toString());
  }

  await fetch(
    `${BACKEND_URL}/assign-gardener-to-sensor-station?${params.toString()}`,
    { method: "POST" }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while assigning gardener to sensor station",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Gardener assigned to sensor station"
      );
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while assigning gardener to sensor station",
        e
      );
      throw error(500, {
        message: "Error while assigning gardener to sensor station",
      });
    });
}
