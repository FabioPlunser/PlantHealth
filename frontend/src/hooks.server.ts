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
  logger.info("Handle request: " + JSON.stringify(event.request));

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
    logger.info("Token found: " + JSON.stringify(event.locals.user));
  } else {
    logger.error("No token found");
    event.locals.user.token = "";
    event.locals.user = undefined;
    const response = await resolve(event);
    return response;
  }
  // console.log("CurrentUser: ")
  // console.table(event.locals.user);
  logger.info("User: " + JSON.stringify(event.locals.user));

  if (event.url.pathname.startsWith("/login")) {
    if (event.locals.user) {
      throw redirect(302, "/");
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
  console.log(request.headers);

  return fetch(request);
}
// export const handleFetch = (async ({ event }) => {
//logger.info("HandleFetch Event: " + JSON.stringify(event));
// logger.info("HandleFetch request: " + JSON.stringify(event.request));

// // console.log(event.request.url);
// if(event.request.url.includes("/login") || event.request.url.includes("/register")){
//   logger.info("Login fetch");
//   return event.fetch(event.request);
// }

// console.log("HandleFetch", event.locals.user);

// let cookieToken = event.cookies.get("token") || "";
// console.log("HanldeFetch", cookieToken);
// let token = JSON.parse(cookieToken);
// console.log(token);

// event.request.headers.set("Authorization", JSON.stringify(token));
// logger.info("HandleFetch Token is: " + event.request.headers.get("Authorization"));
//   return event.fetch(event.request);
// }) satisfies HandleFetch;

// export const handleError = (({ event, error }) => {
//   logger.error("HandleError Event: " + JSON.stringify(event));
//   logger.error("HandleError Error: " + JSON.stringify(error));

// }) satisfies HandleServerError;
