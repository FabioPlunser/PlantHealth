import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { ErrorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

export async function updateFromTo(event: any) {
  let fromData = await event.request.formData();
  let _from = String(fromData.get("from"));
  let _to = String(fromData.get("to"));

  let from = new Date(_from);
  let to = new Date(_to);

  from.setHours(0, 0, 0, 0);
  to.setHours(23, 59, 59, 999);

  logger.info("Update from to", { payload: { from, to } });

  event.cookies.set("from", from.toISOString(), { path: "/" });
  event.cookies.set("to", to.toISOString(), { path: "/" });
}
