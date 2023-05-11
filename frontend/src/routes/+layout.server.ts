import { BACKEND_URL } from "$env/static/private";
import { toasts } from "$stores/toastStore";
import { logger } from "$helper/logger";

export async function load({ depends, locals }) {
  console.log("layout", locals.user);
  let error = false;

  if (locals.user) {
    return {
      error: null,
      streamed: {
        notifications: new Promise((resolve) => {
          toasts.get(locals.user.personId).subscribe((value) => {
            // logger.info("Got toast for: " + locals.user.personId + ": " + JSON.stringify(value));
            resolve(value);
          });
          // toast.removeToast(locals.user.personId);
        }),
      },
    };
  }
  let res;
  try {
    res = await fetch(`${BACKEND_URL}/get-user-permissions`);
    console.log(res);
  } catch (err) {
    logger.error("layout: get-user-permissions " + JSON.stringify(err));
    if (err.cause.code === "ECONNREFUSED") {
      return {
        error: "Backend is not reachable",
      };
    }
  }

  return { error: null };
}
