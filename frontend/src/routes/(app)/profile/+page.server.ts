import type { Actions, PageServerLoad } from "./$types";
import { fail, redirect } from "@sveltejs/kit";
import { BACKEND_URL } from "$env/static/private";
import { z } from "zod";

let personId: string;
let source: string | null;

export const load = (async ({ url, fetch, locals }) => {
  personId = url.searchParams.get("personId") ?? locals.user.personId;
  source = url.searchParams.get("source");

  let canActiveUserChangeRoles: boolean =
    locals.user.permissions.includes("ADMIN") &&
    personId !== locals.user.personId;

  let userPermissions: { [role: string]: boolean } = {};
  // TODO: fetch proper backend endpoint
  //let user = await fetch(`http://${BACKEND_URL}/get-user-info`);
  let user = {
    personId: "1123iefefsa",
    username: "Sakura",
    email: "sakura@mail.com",
    permissions: ["USER"],
  };

  if (canActiveUserChangeRoles) {
    // TODO: fetch admin get-all-permissions
    let allPermissions = ["USER", "GARDENER", "ADMIN"];
    allPermissions.forEach((permission) => {
      userPermissions[permission.toLowerCase()] =
        user.permissions.includes(permission);
    });
  } else {
    user.permissions.forEach((permission) => {
      userPermissions[permission.toLowerCase()] = true;
    });
  }
  return {
    username: user.username,
    email: user.email,
    permissions: userPermissions,
    canActiveUserChangeRoles,
    source,
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
    .trim(),

  password: z
    .string({ required_error: "Password is required" })
    .min(1, { message: " Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),
});

/*
 * In FormData the permissions will have the following shape if selected in the form:
 * { name: 'permission[PERMISSION]', value: 'on' }
 * in order to access this PERMISSION and add it to the permissions array this regex is used.
 */
const permissionRegex = /\[(.*?)\]/;

export const actions = {
  updateUser: async ({ request, fetch }) => {
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

    let permissions: string[] = [];

    /*
     * For every permission BooleanButton in the form, parse the name to find which Permission it represents
     * and add it to the permissions array
     */
    formData.forEach((value, key) => {
      if (key.includes("permission[")) {
        let match = key.match(permissionRegex);
        if (match) {
          permissions.push(match[1].toUpperCase());
        }
      }
    });

    var requestOptions = {
      method: "POST",
      body: JSON.stringify({
        personId,
        username: formData.get("username"),
        email: formData.get("email"),
        password: formData.get("password"),
        permissions,
      }),
    };

    console.log(requestOptions);

    let res = await fetch(`http://${BACKEND_URL}/update-user`, requestOptions);
    res = await res.json();
    if (res.ok && source !== null) {
      throw redirect(307, source);
    }
  },
} satisfies Actions;
