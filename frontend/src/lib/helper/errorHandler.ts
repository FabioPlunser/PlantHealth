import { logger } from "./logger";
import { toasts } from "$stores/toastStore";

export async function errorHandler(
  personId: string | undefined,
  message: string,
  payload: any
) {
  logger.error(message, { payload: payload });
  toasts.addToast(personId, "error", message);
}
