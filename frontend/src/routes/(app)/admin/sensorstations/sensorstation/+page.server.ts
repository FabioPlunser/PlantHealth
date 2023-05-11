import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

export async function load({ fetch, depends, cookies }) {
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
    .trim(),

  upperLimit: z
    .number({ required_error: "Upper Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  lowerLimit: z
    .number({ required_error: "Lower Limit is required" })
    .positive({ message: "Limit has to be positive" }),

  thresholdDuration: z
    .number({ required_error: "Threshold Duration is required" })
    .positive({ message: "Duration has to be positive" }),
});

export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ request, fetch }) => {
    const formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let unlocked = formData.get("unlocked");

    let params = new URLSearchParams();
    params.set("sensorStationId", sensorStationId?.toString() ?? "");
    params.set("unlocked", unlocked?.toString() ?? "");

    let parametersString = "?" + params.toString();
    await fetch(
      `${BACKEND_URL}/set-unlocked-sensor-station${parametersString}`,
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

  update: async ({ request, fetch }) => {
    const formData = await request.formData();
    const zodData = schema.safeParse(Object.fromEntries(formData));
    let sensorStationId = formData.get("sensorStationId");
    let sensorStationName = formData.get("name");

    // validate name input
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

    await fetch(
      `${BACKEND_URL}/update-sensor-station?sensorStationId=${sensorStationId}`,
      {
        method: "POST",
        body: JSON.stringify({
          limits: [
            {
              sensorId,
              upperLimit,
              lowerLimit,
            },
          ],
        }),
      }
    ).then((response) => {
      let time = new Date().toLocaleString();
      if (!response.ok) {
        console.log(`${time} : ${response.message}`);
        throw error(response.status, response.statusText);
      } else {
        console.log(
          `${time} : set updated sensorstation = ${sensorStationId} with name = ${sensorStationName}`
        );
      }
    });
  },

  setLimit: async ({ request, fetch }) => {
    const formData = await request.formData();
    const zodData = schema.safeParse(Object.fromEntries(formData));
    let sensorStationId = formData.get("sensorStationId");
    let sensorType = formData.get("sensorType");
    let upperLimit = formData.get("upperLimit");
    let lowerLimit = formData.get("lowerLimit");

    // validate name input
    if (!zodData.success) {
      console.log("fail");
      // Loop through the errors array and create a custom errors array
      const errors = zodData.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }

    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId?.toString() ?? "");

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/set-sensor-limits${parametersString}`, {
      method: "POST",
      body: JSON.stringify({
        limits: [
          {
            sensorId,
            upperLimit,
            lowerLimit,
          },
        ],
      }),
    }).then((response) => {
      let time = new Date().toLocaleString();
      if (!response.ok) {
        console.log(`${time} : ${response.message}`);
        throw error(response.status, response.statusText);
      } else {
        console.log(
          `${time} : set limits for sensorstation = ${sensorStationId} sensor = ${sensorType} to {upperLimit = ${upperLimit}, lowerLimit ${lowerLimit}}`
        );
      }
    });
  },

  delete: async ({ request, fetch, locals }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");
    let params = new URLSearchParams();
    params.set("sensorStationnId", sensorStationId?.toString() ?? "");

    let parametersString = "?" + params.toString();

    await fetch(`${BACKEND_URL}/delete-sensor-station${parametersString}`, {
      method: "DELETE",
    })
      .then((response) => {
        if (!response.ok) {
          throw error(response.status, response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        console.log(
          `${time} : Admin with id: ${locals.user.personId} and username: ${locals.user.username} deleted sensor station with id: ${sensorStationId}`
        );
      });
  },
};
