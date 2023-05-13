import { redirect } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";

/**
 * This function logs out the user by sending a request to the backend, clearing the token cookie, and
 * redirecting to the login page.
 * @param  - - `locals`: an object containing local variables that can be used in the template
 * rendering process.
 * @returns The function is throwing a redirect error with a status code of 302 and a target URL of
 * "/login". Therefore, nothing is being returned explicitly.
 */
export async function load(event) {
  let res = await event.fetch(`${BACKEND_URL}/logout`, {
    method: "POST",
  });
  logger.info(`User ${event.locals.user.username} logged out`);
  event.cookies.getAll().forEach((cookie) => {
    event.cookies.set(cookie.name, "", { maxAge: 0 });
  });

  throw redirect(302, "/login");
}
