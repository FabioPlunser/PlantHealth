import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { z } from "zod";
import { toasts } from "$lib/stores/toastStore";
import { addToast } from "../../../../lib/components/toast_old/addToToastStore";

// TODO: add validation and error handling (toast messages)
/**
 * This function loads sensor stations from a backend URL and returns them along with a boolean
 * indicating whether the request was made from an access points page.
 * @param  - - `locals`: an object containing local variables for the current request
 * @returns An object with two properties: "fromAccessPoints" and "sensorStations". The
 * "fromAccessPoints" property is a boolean value indicating whether the request was made from the
 * "/admin/accesspoints" page. The "sensorStations" property is an array of sensor station objects
 * obtained from a fetch request to the backend API.
 */
export async function load({ locals, fetch, request, depends, url }) {
  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  if (!res.ok) {
    logger.error("Could not get sensor stations");
    throw error(res.status, "Could not get sensor stations");
  }
  res = await res.json();
  logger.info("Got sensor stations");

  let gardener = await fetch(`${BACKEND_URL}/get-all-gardener`);
  if (!gardener.ok) {
    logger.error("Could not get gardener");
    throw error(gardener.status, "Could not get gardener");
  } else {
    gardener = await gardener.json();
    gardener = gardener.items;
  }

  depends("app:getSensorStations");
  return {
    fromAccessPoints,
    gardener,
    sensorStations: res.sensorStations,
  };
}

const nameSchema = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .min(6, { message: "Name must be at least 6 characters" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim(),
});
export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ request, fetch }) => {
    const formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let unlocked = formData.get("unlocked");

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId?.toString() ?? "");
    params.set("unlocked", unlocked?.toString() ?? "");

    let parametersString = "?" + params.toString();
    await fetch(
      `${BACKEND_URL}/set-unlocked-sensor-station${parametersString}`,
      {
        method: "POST",
      }
    ).then((response) => {
      let time = new Date().toLocaleString();
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        throw error(response.status, response.statusText);
      } else {
        logger.info(
          `unlocked set to = ${unlocked} for sensor station with id = ${sensorStationId}`
        );
      }
    });
  },

  update: async ({ request, fetch, locals }) => {
    const formData = await request.formData();
    const zodData = nameSchema.safeParse(Object.fromEntries(formData));

    // validate name input
    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }

    let sensorStationId: string = String(formData.get("sensorStationId"));
    let sensorStationName: string = String(formData.get("name"));
    let params = new URLSearchParams();

    params.set("sensorStationId", sensorStationId);
    params.set("sensorStationName", sensorStationName);

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/update-sensor-station${parametersString}`, {
      method: "POST",
      body: JSON.stringify({
        limits: [],
      }),
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to update name: ${response.status} ${response.message}`
        );
      } else {
        logger.info(
          `updated sensor station name for sensor station with id = ${sensorStationId} to name = ${sensorStationName}`
        );
        toasts.addToast(
          locals.user.personId,
          "success",
          "Updated sensor station name"
        );
      }
    });

    let gardenerId = formData.get("gardener");
    params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId);
    params.set("gardenerId", gardenerId);
    let res = await fetch(
      `${BACKEND_URL}/assign-gardener-to-sensor-station?${params.toString()}`,
      {
        method: "POST",
      }
    );
    if (!res.ok) {
      logger.error("Could not assign gardener to sensor station");
      throw error(res.status, "Could not assign gardener to sensor station");
    } else {
      logger.info("Assigned gardener to sensor station");
      toasts.addToast(
        locals.user.personId,
        "success",
        "Assigned gardener to sensor station"
      );
    }
  },
  delete: async ({ request, fetch, locals }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId?.toString() ?? "");

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/delete-sensor-station${parametersString}`, {
      method: "DELETE",
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to delete sensor station: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Deleted sensor station = ${sensorStationId}`);
        toasts.addToast(
          locals.user.personId,
          "success",
          "Deleted sensor station"
        );
      }
    });
  },
};
