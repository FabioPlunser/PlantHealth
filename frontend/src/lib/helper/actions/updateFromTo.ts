import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

/**
 * This function updates the "from" and "to" dates based on user input and sets them as cookies.
 * @param {any} event - The "event" parameter is an object that represents the HTTP request event
 * triggered by a client's request to the server. It contains information about the request, such as
 * the request method, headers, and body. In this case, it is used to extract form data submitted by
 * the client and set cookies
 */
export async function updateFromTo(event: any) {
  let fromData = await event.request.formData();
  let _from = String(fromData.get("from"));
  let _to = String(fromData.get("to"));

  let from = new Date(_from);
  let to = new Date(_to);

  from.setHours(0, 0, 0, 0);
  to.setHours(23, 59, 59, 999);

  logger.info("Update from to", { payload: { from, to } });

  event.cookies.set("from", from.toISOString(), { secure: false, path: "/" });
  event.cookies.set("to", to.toISOString(), { secure: false, path: "/" });
}
