import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

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

/**
 * This function updates the limit values for a sensor station and sends a POST request to the backend
 * API.
 * @param {any} event - The `event` parameter is an object that contains information about the HTTP
 * request that triggered the function, including the request object and the fetch function used to
 * make HTTP requests.
 * @returns either a fail response with a status code of 400 and an object containing an error message
 * and an array of errors, or it is not returning anything (i.e., returning undefined) if the fetch
 * request is successful. However, if the fetch request fails, it throws an error with a status code of
 * 500 and an object containing an error message.
 */
export async function updateLimit(event: any) {
  const { request, fetch } = event;
  const formData = await request.formData();
  const zodData = limitsSchema.safeParse({
    upperLimit: Number(formData.get("upperLimit")),
    lowerLimit: Number(formData.get("lowerLimit")),
    thresholdDuration: Number(formData.get("thresholdDuration")),
  });

  let sensorStationId: string = String(formData.get("sensorStationId"));
  let sensor: string = String(formData.get("sensor"));

  if (!zodData.success) {
    // Loop through the errors array and create a custom errors array
    const errors = zodData.error.errors.map((error) => {
      return {
        field: error.path[0],
        message: error.message,
      };
    });

    return fail(400, { id: sensor, error: true, errors });
  }

  let upperLimit: number = Number(formData.get("upperLimit"));
  let lowerLimit: number = Number(formData.get("lowerLimit"));
  let thresholdDuration: number = Number(formData.get("thresholdDuration"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);

  let requestOptions = {
    method: "POST",
    body: JSON.stringify([
      {
        upperLimit,
        lowerLimit,
        thresholdDuration,
        sensor: {
          type: sensor,
        },
      },
    ]),
    headers: {
      "Content-Type": "application/json",
    },
  };

  await fetch(
    `${BACKEND_URL}/update-sensor-station?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while updating limit",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(event.locals.user?.personId, "success", "Limit updated");
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while updating limit",
        e
      );
      throw error(500, { message: "Error while updating limit" });
    });
}
