import type { Actions } from "./$types";
import { fail, redirect, error } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { z } from "zod";
import { logger } from "$helper/logger";
import { toasts } from "$lib/stores/toastStore";

let personId: string | undefined;
let source: string | null;

/**
 * This function loads user permissions and other information based on the URL and user data.
 * @param  - - `request`: an object representing the incoming HTTP request
 * @returns An object with the properties `username`, `permissions`, and `canActiveUserChangeRoles`.
 * The `username` property is set to the value of the `username` query parameter or the `username`
 * property of the `locals.user` object if the query parameter is not provided. The `permissions`
 * property is an object with boolean values for each permission role, based on whether the user has
 * that
 */
export async function load(event) {
  const { fetch, request, url } = event;
  // check if required variables are available
  if (
    (!url.searchParams.get("personId") && !event.locals?.user?.personId) ||
    (!url.searchParams.get("username") && !event.locals?.user?.username) ||
    (!url.searchParams.get("userPermissions") &&
      !event.locals?.user?.permissions)
  ) {
    throw error(403, "Missing required parameters");
  }

  personId = url.searchParams.get("personId") ?? event.locals.user?.personId;
  let username =
    url.searchParams.get("username") ?? event.locals.user?.username;
  source = request.headers.get("referer");

  logger.info("user-profile-page", { payload: personId });
  logger.info("user-profile-page", { payload: username });
  logger.info("user-profile-page", { payload: source });

  let permissions =
    url.searchParams.get("userPermissions")?.split(",") ??
    event.locals.user?.permissions ??
    [];

  let canActiveUserChangeRoles: boolean | undefined =
    event.locals.user?.permissions.includes("ADMIN") &&
    personId !== event.locals.user?.personId;

  let userPermissions: { [role: string]: boolean } = {};

  logger.info("user-profile-page", { permissions });

  if (canActiveUserChangeRoles) {
    logger.info("can change roles");
    await fetch(`${BACKEND_URL}/get-all-permissions`)
      .then((response) => {
        if (!response.ok) {
          logger.error("user-profile-page", { payload: response });
          throw error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        logger.info("user-profile-page", { payload: data });
        data.items.forEach((permission: string) => {
          userPermissions[permission.toLowerCase()] =
            permissions.includes(permission);
        });
      });
  } else {
    permissions.forEach((permission) => {
      userPermissions[permission.toLowerCase()] = true;
    });
  }

  return {
    username,
    permissions: userPermissions,
    canActiveUserChangeRoles,
  };
}

const schema = z.object({
  username: z
    .string({ required_error: "Username is required" })
    .min(1, { message: "Username is required" })
    .max(64, { message: "Username must be less than 64 characters" })
    .trim(),

  email: z
    .string({ required_error: "Email is required" })
    .email({ message: "Email must be a valid email address" })
    .min(1, { message: "Email is required" })
    .max(64, { message: "Email must be less than 64 characters" })
    .trim()
    .or(z.literal("")),

  password: z
    .string({ required_error: "Password is required" })
    .min(1, { message: " Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim()
    .or(z.literal("")),

  passwordConfirm: z
    .string({ required_error: "Password is required" })
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim()
    .or(z.literal("")),
});

/*
 * In FormData the permissions will have the following shape if selected in the form:
 * { name: 'permission[PERMISSION]', value: 'on' }
 * in order to access this PERMISSION and add it to the permissions array this regex is used.
 */
const permissionRegex = /\[(.*?)\]/;

export const actions = {
  /* `updateUser` is an action function that is responsible for updating user information based on the
  form data submitted by the user. It receives an object with properties `url`, `request`, and
  `fetch` as its argument. */
  updateUser: async (event) => {
    const { url, request, fetch } = event;
    const formData = await request.formData();
    const zodData = schema.safeParse(Object.fromEntries(formData));
    if (formData.get("password") !== formData.get("passwordConfirm")) {
      return fail(400, { error: true, errors: "Passwords do not match" });
    }

    if (!zodData.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }
    /*
     *  NOTE: For every permission BooleanButton in the form, parse the name to find which Permission it represents
     * and add it to the permissions array
     */
    let permissions: string[] = [];
    formData.forEach((value, key) => {
      if (key.includes("permission[")) {
        let match = key.match(permissionRegex);
        if (match) {
          permissions.push(match[1].toUpperCase());
        }
      }
    });

    // NOTE: Cancel submit if no role was selected
    if (permissions.length < 1) {
      toasts.addToast(event.locals.user?.personId, "error", "No role selected");
      return;
    }

    let username = String(formData.get("username"));
    let email = String(formData.get("email"));
    let password = String(formData.get("password"));

    let canActiveUserChangeRoles: boolean | undefined =
      event.locals.user?.permissions.includes("ADMIN") &&
      personId !== event.locals.user?.personId;

    let params = new URLSearchParams();
    params.set("personId", personId ?? ""); // NOTE: if personId is not present pass empty string to fail fetch
    params.set("username", username);

    if (canActiveUserChangeRoles) {
      params.set("permissions", permissions.join(","));
    }

    logger.info(
      "Update user " +
        JSON.stringify(username) +
        ": " +
        JSON.stringify(permissions) +
        " " +
        JSON.stringify(email) +
        " " +
        JSON.stringify(password)
    );

    if (email !== "") {
      params.set("email", email);
    }

    if (password !== "") {
      params.set("password", password);
    }

    let parametersString = "?" + params.toString();

    /*
     * Depending on the permission of the current user and if he is supposed to change the roles
     * of the displayed user we fetch a different endpoint
     */
    if (canActiveUserChangeRoles) {
      await fetch(`${BACKEND_URL}/update-user` + parametersString, {
        method: "POST",
      })
        .then((response) => {
          if (!response.ok) {
            logger.error("user-profile-page", { payload: response });
            throw error(response.status, response.statusText);
          }
          return response.json();
        })
        .then((data) => {
          logger.info("Updated user: " + JSON.stringify(data.message));
        });
    } else {
      await fetch(`${BACKEND_URL}/update-settings` + parametersString, {
        method: "POST",
      })
        .then((response) => {
          if (!response.ok) {
            logger.error("user-profile-page", { payload: response });
            throw error(response.status, response.statusText);
          }
          return response.json();
        })
        .then((data) => {
          logger.info("Updated user: " + JSON.stringify(data.message));
          throw redirect(307, "/logout");
        });
    }

    // NOTE: Redirect if the user was redirected to profile from some other page
    if (source !== null) {
      throw redirect(307, source);
    }
  },
};
