import type { Handle, HandleFetch, HandleServerError } from "@sveltejs/kit";
import { redirect, error } from "@sveltejs/kit";

/**
 * @type {Handle}
 * Check if user is logged in and has the correct permissions
 * Redirect to login if not logged in
 * Redirect to home if logged in but does not have the correct permissions
 * Add user to event.locals
 */
export const handle = (async ({ event, resolve, locals }) => {
  const { cookies } = event;
  let token = cookies.get("token");

  if (token) {
    token = JSON.parse(token);
    event.locals.user = token;
  } else {
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
  // console.log("handleFetch");
  const { cookies } = event;
  // console.log("event", event)
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

  console.log("request", request.headers);
  return fetch(request);
}) satisfies HandleFetch;
