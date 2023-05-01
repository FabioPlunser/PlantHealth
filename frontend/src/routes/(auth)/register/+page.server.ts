import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect } from "@sveltejs/kit";
import { z } from "zod";

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

export const actions = {
  register: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    const zod = schema.safeParse(Object.fromEntries(formData));
    console.log(formData);

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

    var requestOptions = {
      method: "POST",
      body: formData,
    };

    let res = await fetch(
      `http://${BACKEND_URL}/register`,
      requestOptions
    ).catch((error) => console.log("error", error));

    res = await res.json();
    console.log(res);

    if (res.success) {
      cookies.set(
        "token",
        JSON.stringify({
          token: res.token,
          permissions: res.permissions,
          personId: res.personId,
        })
      );
      throw redirect(302, "/");
    } else {
      // TODO: add to toast notifications.
      return fail(400, { error: true, errors: res.message });
    }
  },
} satisfies Actions;
