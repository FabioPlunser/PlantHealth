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

  password: z
    .string({ required_error: "Password is required" })
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),
});

export const actions = {
  login: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    const zodData = schema.safeParse(Object.fromEntries(formData));

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

    var requestOptions = {
      method: "POST",
      body: formData,
    };

    let res = null;
    try {
      res = await fetch(`http://${BACKEND_URL}/login`, requestOptions);
    } catch (error) {
      console.log("login", error);
      return fail(503, { message: "Server connection refused" });
      return { success: false };
    }

    if (res.status >= 200 && res.status < 300) {
      res = await res.json();
      console.log("login", res);
      cookies.set(
        "token",
        JSON.stringify({
          token: res.token,
          username: formData.get("username"),
          permissions: res.permissions,
          personId: res.personId,
        })
      );
      throw redirect(302, "/");
    } else {
      return fail(401, { message: res.message });
    }
  },
} satisfies Actions;
