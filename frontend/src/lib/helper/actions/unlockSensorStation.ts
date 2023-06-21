import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

export async function unlockSensorStation(event: any) {
  const { request, fetch } = event;
  const formData = await request.formData();
  let sensorStationId = String(formData.get("sensorStationId"));
  let unlocked = String(formData.get("unlocked"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);
  params.set("unlocked", unlocked);

  await fetch(
    `${BACKEND_URL}/set-unlocked-sensor-station?${params.toString()}`,
    {
      method: "POST",
    }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Couldn't unlock sensor station",
          await res.json()
        );
      } else {
        logger.info(
          `Sensor station unlocked set to '${unlocked}' for id = ${sensorStationId}`
        );
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          unlocked === "true"
            ? "sensor station unlocked"
            : "sensor station locked"
        );
      }
    })
    .catch((err: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Couldn't unlock sensor station",
        err
      );
      throw error(500, "Couldn't unlock sensor station");
    });
}
