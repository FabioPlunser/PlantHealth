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

export async function updateSensorStation(event: any) {
  const { request, fetch } = event;
  const formData = await request.formData();
  const zodData = nameSchema.safeParse({
    name: formData.get("name"),
    transferInterval: Number(formData.get("transferInterval")),
  });

  let sensorStationId: string = String(formData.get("sensorStationId"));

  // validate name input
  if (!zodData.success) {
    // Loop through the errors array and create a custom errors array
    const errors = zodData.error.errors.map((error) => {
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
  await fetch(
    `${BACKEND_URL}/update-sensor-station?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while updating sensor station",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Sensor station updated"
      );
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while updating sensor station",
        e
      );
      throw error(500, "Error while updating sensor station");
    });
}
