// import toast, {toasts} from "$components/Toast"
import { get } from "svelte/store";
import { toasts } from "$stores/toastStore";
import { logger } from "$helper/logger";

export function load({ depends, locals }) {
  console.log("layout", locals.user);
  if (locals.user) {
    return {
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
  } else {
    return {
      streamed: {
        notifications: new Promise((resolve) => {
          resolve(null);
        }),
      },
    };
  }
}
