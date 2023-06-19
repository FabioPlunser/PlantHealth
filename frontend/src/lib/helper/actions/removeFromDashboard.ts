import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

/**
 * This is an async function that removes a sensor station from a dashboard by sending a POST request
 * to a backend URL.
 * @param {any} event - The event parameter is an object that contains information about the event that
 * triggered the function, such as the request and fetch objects.
 */
export async function removeFromDashboard(event: any) {
  const { request, fetch } = event;
  let formdData = await request.formData();
  let sensorStationId: string = String(formdData.get("sensorStationId"));

  await fetch(
    `${BACKEND_URL}/remove-from-dashboard?sensorStationId=${sensorStationId}`,
    { method: "POST" }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while removing from dashboard",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Removed from dashboard"
      );
    })
    .catch((err: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while removing from dashboard",
        err
      );
      throw error(500, "Error while removing from dashboard");
    });
}
