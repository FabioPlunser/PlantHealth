import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect, error } from "@sveltejs/kit";
import { z } from "zod";
import { logger } from "$helper/logger";
import { errorHandler } from "$helper/errorHandler";

export async function load(event) {
  const { fetch } = event;

  async function getUsers(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-all-users`)
        .then(async (res) => {
          if (!res.ok) {
            res = await res.json();
            errorHandler(
              event.locals.user?.personId,
              "Error while getting users",
              res
            );
            reject(res);
          }
          let data = await res.json();
          resolve(data.items);
        })
        .catch((err) => {
          errorHandler(
            event.locals.user?.personId,
            "Error while getting users",
            err
          );
          reject(err);
          throw error(500, "Error while getting users");
        });
    });
  }

  return {
    streamed: {
      users: getUsers(),
    },
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
  createUser: async (event) => {
    const { request, fetch } = event;
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
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while creating user",
            res
          );
        }
      })
      .catch((err) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while creating user",
          err
        );
        throw error(500, "Error while creating user");
      });
  },
  //---------------------------------------------
  //
  //---------------------------------------------
  deleteUser: async (event) => {
    const { request, fetch } = event;
    const formData = await request.formData();
    let personId = formData.get("personId");

    let params = new URLSearchParams();
    params.set("personId", personId?.toString() ?? "");

    await fetch(`${BACKEND_URL}/delete-user?${params.toString()}`, {
      method: "DELETE",
    })
      .then(async (res) => {
        if (!res.ok) {
          res = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while deleting user",
            res
          );
        }
      })
      .catch((err) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while deleting user",
          err
        );
        throw error(500, "Error while deleting user");
      });
  },
};
