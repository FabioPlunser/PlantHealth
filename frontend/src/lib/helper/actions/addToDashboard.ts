import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

/**
 * This function adds a sensor station to the dashboard by sending a POST request to the backend API.
 * @param {any} event - The event parameter is an object that contains information about the HTTP
 * request that triggered the function, including the request object and the fetch function.
 */
export async function addToDashboard(event: any) {
  const { request, fetch } = event;
  let formdData = await request.formData();
  let sensorStationId: string = String(formdData.get("sensorStationId"));

  await fetch(
    `${BACKEND_URL}/add-to-dashboard?sensorStationId=${sensorStationId}`,
    { method: "POST" }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while adding to dashboard",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Added to dashboard"
      );
    })
    .catch((err: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while adding to dashboard",
        err
      );
      throw error(500, "Error while adding to dashboard");
    });
}
