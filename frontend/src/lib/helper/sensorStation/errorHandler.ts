import { logger } from "../logger";
import { toasts } from "$stores/toastStore";

export async function ErrorHandler(
  personId: string,
  message: string,
  payload: any
) {
  logger.error(message, { payload: payload });
  toasts.addToast(personId, "error", message);
}
