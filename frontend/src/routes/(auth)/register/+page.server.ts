import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect } from "@sveltejs/kit";
import { z } from "zod";
import { logger } from "$helper/logger";

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
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),

  passwordConfirm: z
    .string({ required_error: "Password is required" })
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),
});

/* This code exports an object named `actions` that contains a single function named `register`. This
function is an asynchronous function that takes an object with three parameters: `cookies`,
`request`, and `fetch`. */
export const actions = {
  register: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    const zod = schema.safeParse(Object.fromEntries(formData));

    if (formData.get("password") !== formData.get("passwordConfirm")) {
      return fail(400, { error: true, errors: "Passwords do not match" });
    }

    if (!zod.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zod.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }
    formData.delete("passwordConfirm");

    var requestOptions = {
      method: "POST",
      body: formData,
    };

    let res = await fetch(`${BACKEND_URL}/register`, requestOptions).catch(
      (error) => {
        logger.error(`User ${formData.get("username")} failed to register`, {
          error,
        });
      }
    );

    res = await res.json();
    logger.info(`User ${formData.get("username")} registered`);

    if (res.success) {
      cookies.set(
        "token",
        JSON.stringify({
          token: res.token,
          username: formData.get("username"),
          permissions: res.permissions,
          personId: res.personId,
        })
      );
      throw redirect(302, "/user");
    } else {
      return fail(400, { error: true, errors: res.message });
    }
  },
} satisfies Actions;
