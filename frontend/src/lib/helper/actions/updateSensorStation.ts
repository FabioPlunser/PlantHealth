import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

const nameSchema = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim(),
});

/**
 * This is an async function that updates a sensor station with data from a form submission.
 * @param {any} event - The event parameter is an object that contains information about the HTTP
 * request that triggered the function, such as the request method, headers, and body.
 * @param {any | undefined} formData - formData is an object that contains the data submitted through a
 * form. In this case, it is being passed as a parameter to the updateSensorStation function and is
 * used to extract the values of the form fields such as name and transferInterval. If formData is
 * undefined, the function will use the request object
 * @returns either a response with a status code of 400 and an error object containing validation
 * errors, or it is returning a success message indicating that the sensor station was updated.
 */
export async function updateSensorStation(
  event: any,
  formData: any | undefined
) {
  const { request, fetch } = event;
  if (!formData) {
    formData = await request.formData();
  }
  const zodData = nameSchema.safeParse({
    name: formData.get("name"),
    transferInterval: Number(formData.get("transferInterval")),
  });

  let sensorStationId: string = String(formData.get("sensorStationId"));

  // validate name input
  if (!zodData.success) {
    // Loop through the errors array and create a custom errors array
    const errors = zodData.error.errors.map((error) => {
      toasts.addToast(event.locals.user?.personId, "error", error.message);
      return {
        field: error.path[0],
        message: error.message,
      };
    });

    return fail(400, { id: sensorStationId, error: true, errors });
  }

  let sensorStationName: string = String(formData.get("name"));
  let transferInterval: number = Number(formData.get("transferInterval"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);
  params.set("sensorStationName", sensorStationName);
  params.set("transferInterval", JSON.stringify(transferInterval));

  let requestOptions = {
    method: "POST",
    body: JSON.stringify([]),
    headers: {
      "Content-Type": "application/json",
    },
  };

  let message = "Error while updating sensor station";
  await fetch(
    `${BACKEND_URL}/update-sensor-station?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        let data = await res.json();
        errorHandler(event.locals.user?.personId, data.message, data);
        message = data.message;
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Sensor station updated"
      );
    })
    .catch((e: any) => {
      errorHandler(event.locals.user?.personId, message, e);
      // throw error(500, {message: "Error while updating sensor station"});
    });
}
