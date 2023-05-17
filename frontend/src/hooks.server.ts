import { redirect, error } from "@sveltejs/kit";
import { logger } from "$helper/logger";
/**
 * @type {Handle}
 * Check if user is logged in and has the correct permissions
 * Redirect to login if not logged in
 * Redirect to home if logged in but does not have the correct permissions
 * Add user to event.locals
 */
export async function handle({ event, resolve }) {
  logger.info("Handle request: ", { payload: event });

  let cookieToken = event.cookies.get("token") || "";

  if (!event.locals.user) {
    event.locals.user = {
      personId: "",
      username: "",
      permissions: [],
      token: "",
    };
  }

  if (cookieToken !== "") {
    event.locals.user = JSON.parse(cookieToken);
    logger.info("Token found: ", { payload: event.locals.user });
  } else {
    logger.error("No token found");
    event.locals.user.token = "";
    event.locals.user = undefined;
    const response = await resolve(event);
    return response;
  }
  logger.info("User: ", { payload: event.locals.user });

  if (event.url.pathname.startsWith("/login")) {
    if (event.locals.user.token !== "") {
      throw redirect(307, "/");
    }
  }

  if (event.url.pathname.startsWith("/admin")) {
    if (!event.locals.user) {
      throw redirect(307, "/logout");
    }
    if (!event.locals.user.permissions.includes("ADMIN")) {
      throw redirect(307, "/");
    }
  }
  if (event.url.pathname.startsWith("/gardener")) {
    if (!event.locals.user) {
      throw redirect(307, "/logout");
    }
    if (!event.locals.user.permissions.includes("GARDENER")) {
      throw redirect(307, "/");
    }
  }
  if (event.url.pathname.startsWith("/user")) {
    if (!event.locals.user) {
      throw redirect(307, "/logout");
    }
    if (!event.locals.user.permissions.includes("USER")) {
      throw redirect(307, "/");
    }
  }

  const response = await resolve(event);
  return response;
}

/**
 * @type {HandleFetch}
 * Add token to all backend fetches
 */
export async function handleFetch({ request, fetch, event }) {
  if (!event.locals.user) {
    return fetch(request);
  }

  let token = {
    token: event.locals.user.token,
    username: event.locals.user.username,
  };

  request.headers.set("Authorization", JSON.stringify(token));

  return fetch(request);
}

export async function handleError({ error, event }) {
  logger.error("HandleError Event: ", { payload: error.message });
  return {
    message: error.message,
    errorId: error.errorId,
  };
}
