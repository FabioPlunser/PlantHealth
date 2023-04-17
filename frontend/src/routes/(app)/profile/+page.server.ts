import type { Actions } from "./$types";
import { fail } from "@sveltejs/kit";
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
  profile: async ({ request }) => {
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
  },
} satisfies Actions;

export async function load({ params, url }) {
  let userId = url.searchParams.get("userId");

  // TODO: fetch proper backend endpoint
  let username: string = "Sakura";
  let userEmail: string = "sakura.tree@mail.com";
  let userPassword: string = "1stPasswdOfSakura";
  let userPermissions: { [role: string]: boolean } = {
    user: true,
    gardener: true,
    admin: false,
  };
  let isActiveUserAdmin: boolean = false;

  return {
    username,
    userEmail,
    userPassword,
    userPermissions,
    isActiveUserAdmin,
  };
}
