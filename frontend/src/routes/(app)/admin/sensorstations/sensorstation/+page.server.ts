import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";
import { toasts } from "$stores/toastStore";

export async function load({ fetch, depends, cookies }) {
  let sensorStationId = cookies.get("sensorStationId");

  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  if (!res.ok) {
    logger.error("Could not get sensor station");
    throw error(res.status, "Could not get sensor station");
  }

  res = await res.json();
  logger.info("Got sensor station");
  depends("app:getSensorStation");
  return res;
}

const nameSchema = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .min(6, { message: "Name must be at least 6 characters" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim(),
});

const limitsSchema = z.object({
  upperLimit: z
    .number({ required_error: "Upper Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  lowerLimit: z
    .number({ required_error: "Lower Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  thresholdDuration: z
    .number({ required_error: "Threshold Duration is required" })
    .positive({ message: "Duration has to be positive" }),
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

  updateName: async ({ request, fetch, locals }) => {
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
    params.set("sensorStationnId", sensorStationId);
    params.set("sensorStationName", sensorStationName);

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/updata-sensor-station${parametersString}`, {
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
  },

  updateLimit: async ({ request, fetch, locals }) => {
    const formData = await request.formData();
    const zodData = limitsSchema.safeParse(Object.fromEntries(formData));

    // validate limit input
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
    let sensor: Sensor | null = formData.get("sensor");
    let upperLimit: number = parseFloat(String(formData.get("upperLimit")));
    let lowerLimit: number = parseFloat(String(formData.get("lowerLimit")));
    let thresholdDuration: number = parseFloat(
      String(formData.get("thresholdDuration"))
    );

    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId);

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/updata-sensor-station${parametersString}`, {
      method: "POST",
      body: JSON.stringify({
        limits: [
          {
            upperLimit,
            lowerLimit,
            thresholdDuration,
            sensor,
          },
        ],
      }),
    }).then((response) => {
      if (!response.ok) {
        logger.error("sensor-station-page", { response });
        toasts.addToast(
          locals.user.personId,
          "error",
          `Failed to update limits: ${response.status} ${response.message}`
        );
      } else {
        logger.info(`Limits updated for ${sensor}`);
        toasts.addToast(locals.user.personId, "success", "Updated limits");
      }
    });
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
