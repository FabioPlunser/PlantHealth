import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { redirect, error } from "@sveltejs/kit";

export async function load(event) {
  try {
    await fetch(`${BACKEND_URL}/get-sensor-station-info`);
  } catch (err: any) {
    logger.error("Backend is down", { payload: err });
    if (err.cause.code === "ECONNREFUSED") {
      throw error(503, { message: "Backend not reachable" });
    }
  }

  if (event.locals.user) {
    return {
      permission: event.locals.user.permissions[0],
    };
  }

  return {
    permission: "GUEST",
  };
}
