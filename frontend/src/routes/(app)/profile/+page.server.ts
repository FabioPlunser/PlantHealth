import type { Actions } from "./$types";
import { fail, redirect, error } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { z } from "zod";
import { logger } from "$helper/logger";

let personId: string;
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
export async function load({ request, url, fetch, locals }) {
  // check if required variables are available
  if (	(!url.searchParams.get("personId") && !locals?.user?.personId) ||
		(!url.searchParams.get("username") && !locals?.user?.username) ||
		(!url.searchParams.get("userPermissions") && !locals?.user?.permissions)) {
			throw new error(403);
		}
  
  personId = url.searchParams.get("personId") ?? locals?.user?.personId;
  let username = url.searchParams.get("username") ?? locals.user.username;
  source = request.headers.get("referer");

  logger.info("user-profile-page", { personId });
  logger.info("user-profile-page", { username });
  logger.info("user-profile-page", { source });

  let permissions =
    url.searchParams.get("userPermissions")?.split(",") ??
    locals.user.permissions;

  let canActiveUserChangeRoles: boolean =
    locals.user.permissions.includes("ADMIN") &&
    personId !== locals.user.personId;

  let userPermissions: { [role: string]: boolean } = {};

  logger.info("user-profile-page", { permissions });

  if (canActiveUserChangeRoles) {
    logger.info("Change roles");
    await fetch(`${BACKEND_URL}/get-all-permissions`)
      .then((response) => {
        if (!response.ok) {
          logger.error("user-profile-page", { response });
          throw new error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        logger.info("user-profile-page", { data });
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
  updateUser: async ({ url, request, fetch }) => {
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
     * For every permission BooleanButton in the form, parse the name to find which Permission it represents
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

    let username = formData.get("username");
    let email = formData.get("email");
    let password = formData.get("password");

    let params = new URLSearchParams();

    params.set("personId", personId);
    params.set("username", username);
    params.set("permissions", permissions.join(","));

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

    await fetch(`${BACKEND_URL}/update-user` + parametersString, {
      method: "POST",
    })
      .then((response) => {
        if (!response.ok) {
          logger.error("user-profile-page", { response });
          throw error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        logger.info(
          "Updated user: " +
            JSON.stringify(time) +
            " " +
            JSON.stringify(data.message)
        );
      });

    // NOTE: Redirect if the user was redirected to profile from some other page
    if (source !== null) {
      throw redirect(307, source);
    }
  },
} satisfies Actions;
