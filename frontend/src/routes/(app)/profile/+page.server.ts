import type { Actions, PageServerLoad } from "./$types";
import { fail, redirect } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { z } from "zod";

let personId: string;
let username: string;
let permissions: string[];
let source: string | null;

export const load = (async ({ url, fetch, locals }) => {
  personId = url.searchParams.get("personId") ?? locals.user.personId;
  username = url.searchParams.get("username") ?? locals.user.username;
  source = url.searchParams.get("source");
  permissions =
    url.searchParams.get("userPermissions")?.split(",") ??
    locals.user.permissions;

  let canActiveUserChangeRoles: boolean =
    locals.user.permissions.includes("ADMIN") &&
    personId !== locals.user.personId;

  let userPermissions: { [role: string]: boolean } = {};

  if (canActiveUserChangeRoles) {
    await fetch(`http://${BACKEND_URL}/get-all-permissions`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        data.items.forEach((permission: string) => {
          userPermissions[permission.toLowerCase()] =
            permissions.includes(permission);
        });
      })
      .catch((error) => {
        console.error("Error fetching /get-all-permissions", error);
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
}) satisfies PageServerLoad;

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

    let email = formData.get("email");
    let password = formData.get("password");
    let params = new URLSearchParams();

    params.set("personId", personId);
    params.set("username", username);
    params.set("permissions", permissions.join(","));

    if (email !== "") {
      params.set("email", email);
    }

    if (password !== "") {
      params.set("password", password);
    }

    let parametersString = "?" + params.toString();

    await fetch(`http://${BACKEND_URL}/update-user` + parametersString, {
      method: "POST",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        console.log(`${time} : ${data.message}`);
      })
      .catch((error) => {
        console.error("Error fetching /update-user", error);
      });
    // Redirect if the user was redirected to profile from some other page
    if (source !== null) {
      throw redirect(307, source);
    }
  },
} satisfies Actions;
