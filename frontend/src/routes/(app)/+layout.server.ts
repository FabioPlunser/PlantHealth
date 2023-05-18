import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { redirect, error } from "@sveltejs/kit";

export async function load({ url, locals }) {
  if (locals.user) {
    return {
      permission: locals.user.permissions[0],
    };
  }
  try {
    await fetch(`${BACKEND_URL}/get-sensor-station-info`);
  } catch (err: any) {
    logger.error("Backend is down", { payload: err });
    if (err.cause.code === "ECONNREFUSED") {
      throw redirect(307, "/logout");
    }
  }

  return {
    permission: "GUEST",
  };
}
