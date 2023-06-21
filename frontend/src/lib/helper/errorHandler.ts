import { logger } from "./logger";
import { toasts } from "$stores/toastStore";

/**
 * This is an async function that logs an error message and adds a toast notification for a given
 * person ID.
 * @param {string | undefined} personId - A string that represents the ID of the person for whom the
 * error occurred. It is optional and can be undefined.
 * @param {string} message - The error message that will be logged and displayed in a toast
 * notification.
 * @param {any} payload - The `payload` parameter is an optional parameter of type `any`. It can be
 * used to pass additional information or data related to the error being handled. This information can
 * be logged or displayed in the error message to help with debugging and troubleshooting.
 */
export async function errorHandler(
  personId: string | undefined,
  message: string,
  payload: any
) {
  logger.error(message, { payload: payload });
  toasts.addToast(personId, "error", message);
}
