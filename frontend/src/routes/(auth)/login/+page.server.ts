import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect } from "@sveltejs/kit";
import { z } from "zod";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

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

/* This code exports an object named `actions` that contains a single function named `login`. The
`login` function is an asynchronous function that takes an object with three parameters: `cookies`,
`request`, and `fetch`. */
export const actions = {
  login: async (event) => {
    const formData = await event.request.formData();
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

    let res = await event.fetch(`${BACKEND_URL}/login`, requestOptions);

    if (!res.ok) {
      logger.error(`Error while logging in: ${res.status} ${res.statusText}`);
      return fail(500, { message: "Error while logging in" });
    }

    let data = await res.json();

    let username = formData.get("username") || "";
    let newUser: User = {
      personId: data.personId,
      username: username.toString(),
      permissions: data.permissions,
      token: data.token,
    };

    event.locals.user = newUser;
    toasts.addToast(
      event.locals.user.personId,
      "sueccess",
      `User ${username.toString()} logged in`
    );
    event.cookies.set("token", JSON.stringify(event.locals.user));
    logger.info(`User: ${JSON.stringify(newUser)} loggedIn redirect`);
    throw redirect(307, "/");
  },
} satisfies Actions;
