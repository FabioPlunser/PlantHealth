import type { Handle, HandleFetch, HandleServerError } from "@sveltejs/kit";
import { redirect, error } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { v4 as uuidv4 } from "uuid";
/**
 * @type {Handle}
 * Check if user is logged in and has the correct permissions
 * Redirect to login if not logged in
 * Redirect to home if logged in but does not have the correct permissions
 * Add user to event.locals
 */
export const handle = (async ({ event, resolve, request, locals }) => {
  logger.info("Handle Event: " + JSON.stringify(event));
  logger.info("Handle request: " + JSON.stringify(request));

  const { cookies } = event;
  let token = cookies.get("token");

  event.locals.uuid = uuidv4();
  console.log("uuidv4", event.locals.uuid);
  logger.info("handle uuidv4: " + JSON.stringify(event.locals.uuid));

  if (token) {
    token = JSON.parse(token);
    event.locals.user = token;
    console.log(event.locals.user);
  } else {
    logger.error("No token found");
    event.locals.user = null;
    const response = await resolve(event);
    return response;
  }

  if (event.url.pathname.startsWith("/login")) {
    if (event.locals.user) {
      throw redirect(307, "/");
    }
  }

  if (event.url.pathname.startsWith("/admin")) {
    if (!event.locals.user.permissions.includes("ADMIN")) {
      throw redirect(307, "/");
    }
  }

  if (event.url.pathname.startsWith("/gardener")) {
    if (!event.locals.user.permissions.includes("GARDENER")) {
      throw redirect(307, "/");
    }
  }

  if (event.url.pathname.startsWith("/user")) {
    if (!event.locals.user.permissions.includes("USER")) {
      throw redirect(307, "/");
    }
  }

  const response = await resolve(event);
  return response;
}) satisfies Handle;

/**
 * @type {HandleFetch}
 * Add token to all backend fetches
 */
export const handleFetch = (({ event, request, fetch }) => {
  logger.info("HandleFetch Event: " + JSON.stringify(event));
  logger.info("HandleFetch request: " + JSON.stringify(request));

  const { cookies } = event;
  let token = cookies.get("token");
  if (token) {
    token = JSON.parse(token);
  } else {
    return fetch(request);
  }

  let value = {
    token: token.token,
    username: token.username,
  };

  request.headers.set("Authorization", JSON.stringify(value));
  logger.info("HandleFetch Token is: " + request.headers.get("Authorization"));
  return fetch(request);
}) satisfies HandleFetch;

export const handleError = (({ event, error }) => {
  logger.error("HandleError Event: " + JSON.stringify(event));
  logger.error("HandleError Error: " + JSON.stringify(error));
  return error;
}) satisfies HandleServerError;
