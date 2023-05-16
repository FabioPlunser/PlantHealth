import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect, error } from "@sveltejs/kit";
import { z } from "zod";
import { logger } from "$helper/logger";

/**
 * This function fetches all users from a backend URL and returns them as an object.
 * @param  - - `fetch`: a function used to make HTTP requests to the backend server
 * @returns An object with a property `users` that contains an array of `User` objects. The `load`
 * function fetches data from a backend API endpoint and assigns the response data to the `allUsers`
 * variable, which is then returned as part of the object.
 */
export async function load({ fetch, depends }) {
  let allUsers: User[];

  await fetch(`${BACKEND_URL}/get-all-users`)
    .then((response) => {
      if (!response.ok) {
        logger.error("Couldn't get all users", { payload: response });
        throw error(response.status, response.statusText);
      }
      return response.json();
    })
    .then((data) => {
      logger.info("Got all users", { payload: data });
      allUsers = data.items;
    });
  return { users: allUsers };
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
  /* This is a function that creates a new user by sending a POST request to a backend API endpoint. It
 first retrieves the form data from the request using `request.formData()`, then validates the form
 data using a Zod schema. If the form data is invalid, it returns a 400 error with the validation
 errors. If the passwords do not match, it returns a 400 error with a message indicating that the
 passwords do not match. If the form data is valid and the passwords match, it sends a POST request
 to the backend API endpoint with the form data as the request body. If the request is successful,
 it logs a message indicating that a new user has been created. */
  createUser: async ({ request, fetch, locals }) => {
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

    var requestOptions = {
      // change to PUT once backend is up to date
      method: "POST",
      body: formData,
    };

    await fetch(`${BACKEND_URL}/create-user`, requestOptions)
      .then((response) => {
        if (!response.ok) {
          logger.error("Couldn't create user", { payload: response });
          throw error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        logger.info(
          "Created user: " +
            JSON.stringify(time) +
            JSON.stringify(locals.user.personId) +
            JSON.stringify(locals.user.username) +
            JSON.stringify(data)
        );
      });
  },

  /* The `deleteUser` function is a Svelte action that deletes a user by sending a DELETE request to a
 backend API endpoint. It first retrieves the form data from the request using `request.formData()`,
 then gets the `personId` from the form data. It then creates a URLSearchParams object with the
 `personId` as a parameter and appends it to the backend URL. It then sends a DELETE request to the
 backend API endpoint with the URLSearchParams object as the query string and the `personId` as the
 value of the `personId` parameter. If the request is successful, it logs a message indicating that
 the user has been deleted. */
  deleteUser: async ({ request, fetch, locals }) => {
    const formData = await request.formData();
    let personId = formData.get("personId");

    let params = new URLSearchParams();
    params.set("personId", personId?.toString() ?? "");

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/delete-user` + parametersString, {
      method: "DELETE",
    })
      .then((response) => {
        if (!response.ok) {
          logger.error("Couldn't delete user", { payload: response });
          throw error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        logger.info(
          "Deleted user: " +
            JSON.stringify(time) +
            JSON.stringify(locals.user.personId) +
            JSON.stringify(locals.user.username) +
            JSON.stringify(data)
        );
      });
  },
} satisfies Actions;
