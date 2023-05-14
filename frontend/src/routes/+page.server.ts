import { redirect } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

/**
 * This function checks if the user is logged in and has the appropriate permissions, and redirects
 * them to the appropriate page based on their permissions.
 * @param  - - `locals`: an object containing local variables that can be used in the function
 * @returns An object with a property `success` that has a boolean value. The value of `success`
 * depends on the conditions met in the `if` statements. If the user is not logged in or does not have
 * the required permissions, `success` will be `false`. Otherwise, `success` will be `true`. However,
 * since the function throws a redirect before returning anything, the return statement
 */
export async function load(event) {
  // check if user is exists
  if (event.locals.user === undefined) {
    throw redirect(302, "/login");
  }

  // check if user has permissions
  if (
    !event.locals.user.permissions.includes("ADMIN") &&
    !event.locals.user.permissions.includes("GARDENER") &&
    !event.locals.user.permissions.includes("USER")
  ) {
    throw redirect(302, "/login");
  }

  // get user permissions from backend
  let res = await fetch(`${BACKEND_URL}/get-user-permissions`).catch((err) => {
    logger.error("get-user-permissions", { err });
    throw redirect(302, "/logout");
  });
  if (!res.ok) {
    toasts.addToast(
      event.locals.user.personId,
      "error",
      "Error while fetching user permissions"
    );
    logger.error("get-user-permissions", { res });
  }
  if (res.ok) {
    let data = await res.json();
    if (
      event.locals.user.permissions.toString() !== data.permissions.toString()
    ) {
      toasts.addToast(
        event.locals.user.personId,
        "error",
        "User permissions do not match backend permissions"
      );
      logger.error("get-user-permissions", { res });
      throw redirect(302, "/logout");
    }
  }

  //redirect to appropriate page
  if (event.locals.user.permissions.includes("ADMIN")) {
    logger.info(`User ${event.locals.user.username} redirected to admin page`);
    throw redirect(307, "/admin");
  }
  if (event.locals.user.permissions.includes("GARDENER")) {
    logger.info(
      `User ${event.locals.user.username} redirected to gardener page`
    );
    throw redirect(307, "/gardener");
  }
  if (event.locals.user.permissions.includes("USER")) {
    logger.info(`User ${event.locals.user.username} redirected to user page`);
    throw redirect(307, "/user");
  }

  return { success: true };
}
