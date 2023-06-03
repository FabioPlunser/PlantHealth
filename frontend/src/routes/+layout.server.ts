import { BACKEND_URL } from "$env/static/private";
import { toasts } from "$stores/toastStore";
import { logger } from "$helper/logger";

export async function load(event) {
  let error = false;

  if (event.locals.user) {
    return {
      error: null,
      streamed: {
        notifications: new Promise((resolve) => {
          toasts.get(event.locals.user?.personId).subscribe((value: any) => {
            resolve(value);
          });
          toasts.removeToast(event.locals.user?.personId);
        }),
      },
    };
  }
  try {
    await fetch(`${BACKEND_URL}/get-sensor-station-info`);
  } catch (err: any) {
    logger.error("Backend is down", { payload: err });
    if (err.cause.code === "ECONNREFUSED") {
      event.cookies.getAll().forEach((cookie) => {
        event.cookies.set(cookie.name, "", { secure: false, maxAge: 0 });
      });
      return {
        error: "Backend is not reachable",
      };
    }
  }

  return { error: null };
}
