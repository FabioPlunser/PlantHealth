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
export async function load({ locals, cookies }) {
  let res = await fetch(`${BACKEND_URL}/logout`);
  res = await res.json();
  logger.info(`User ${locals.user.username} logged out`);

  cookies.set("token", "");

  throw redirect(302, "/login");

  return { success: true };
}
