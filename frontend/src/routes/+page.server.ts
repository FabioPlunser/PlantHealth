import { redirect } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";

/**
 * This function checks if the user is logged in and has the appropriate permissions, and redirects
 * them to the appropriate page based on their permissions.
 * @param  - - `locals`: an object containing local variables that can be used in the function
 * @returns An object with a property `success` that has a boolean value. The value of `success`
 * depends on the conditions met in the `if` statements. If the user is not logged in or does not have
 * the required permissions, `success` will be `false`. Otherwise, `success` will be `true`. However,
 * since the function throws a redirect before returning anything, the return statement
 */
export async function load({ locals, fetch }) {
  if (!locals.user) {
    throw redirect(302, "/login");
    return { success: false };
  } else {
    if (
      !locals.user.permissions.includes("ADMIN") &&
      !locals.user.permissions.includes("GARDENER") &&
      !locals.user.permissions.includes("USER")
    ) {
      throw redirect(302, "/login");
      return { success: false };
    }

    // get user permissions from backend
    let res = await fetch(`${BACKEND_URL}/get-user-permissions`).catch(
      (err) => {
        logger.error("get-user-permissions", { err });
        throw redirect(302, "/logout");
      }
    );

    logger.info("get-user-permissions", { res });

    if (res.status >= 200 && res.status < 300) {
      res = await res.json();
      console.log("get-user-permissions", res);
      if (locals.user.permissions.toString() !== res.permissions.toString()) {
        logger.error("User permissions do not match backend permissions");
        throw redirect(302, "/logout");
      }
    }
    // redirect to according pag
    if (locals.user.permissions.includes("ADMIN")) {
      logger.info("redirecting to admin page");
      throw redirect(307, "/admin");
    }

    if (locals.user.permissions.includes("GARDENER")) {
      logger.info("redirecting to gardener page");
      throw redirect(307, "/gardener");
    }

    if (locals.user.permissions.includes("USER")) {
      logger.info("redirecting to user page");
      throw redirect(307, "/user");
    }
  }

  return { success: true };
}
