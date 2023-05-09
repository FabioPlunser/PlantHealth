import { BACKEND_URL } from "$env/static/private";
import { error } from "@sveltejs/kit";
import { z } from "zod";

export async function load({ fetch, depends, url, cookies }) {
  let sensorStationId = cookies.get("sensorStationId");

  let res = await fetch(
    `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
  );
  res = await res.json();

  depends("app:getSensorStation");
  return res;
}

const schema = z.object({
  name: z
    .string({ required_error: "Name is required" })
    .min(1, { message: "Name is required" })
    .min(6, { message: "Name must be at least 6 characters" })
    .max(32, { message: "Name must be less than 32 characters" })
    .trim()
    .or(z.literal("")),
});

export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let unlocked = formData.get("unlocked");
    await fetch(
      `${BACKEND_URL}/set-unlocked-sensor-station?sensorStationId=${sensorStationId}&unlocked=${unlocked}`,
      {
        method: "POST",
      }
    ).then((response) => {
      let time = new Date().toLocaleString();
      if (!response.ok) {
        console.log(`${time} : ${response.message}`);
        throw error(response.status, response.statusText);
      } else {
        console.log(
          `${time} : unlocked set to "${unlocked}" for sensorStation with id = ${sensorStationId}`
        );
      }
    });
  },

  update: async ({ cookies, request, fetch }) => {
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

  setLimits: async ({ cookies, request, fetch }) => {
    let formData = await request.formData();
  },
};
