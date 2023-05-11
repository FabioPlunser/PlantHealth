import { BACKEND_URL } from "$env/static/private";
import { error } from "@sveltejs/kit";
import { logger } from "$helper/logger";

// TODO: add validation and error handling (toast messages)
/**
 * This function loads sensor stations from a backend URL and returns them along with a boolean
 * indicating whether the request was made from an access points page.
 * @param  - - `locals`: an object containing local variables for the current request
 * @returns An object with two properties: "fromAccessPoints" and "sensorStations". The
 * "fromAccessPoints" property is a boolean value indicating whether the request was made from the
 * "/admin/accesspoints" page. The "sensorStations" property is an array of sensor station objects
 * obtained from a fetch request to the backend API.
 */
export async function load({ locals, fetch, request, depends, url }) {
  let referer = request.headers.get("referer");
  let origin = url.origin;
  let fromAccessPoints = false;

  if (referer === `${origin}/admin/accesspoints`) {
    fromAccessPoints = true;
  }

  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  if (!res.ok) {
    logger.error("Could not get sensor stations");
    throw error(res.status, "Could not get sensor stations");
  }
  res = await res.json();
  logger.info("Got sensor stations");

  depends("app:getSensorStations");
  return {
    fromAccessPoints,
    sensorStations: res.sensorStations,
  };
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
    );
    if (!res.ok) {
      logger.error("Could not unlock sensor station");
      throw error(res.status, "Could not unlock sensor station");
    } else {
      logger.info("Unlocked sensor station");
    }
  },

  // TODO: add validation and error handling (toast messages)
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

  delete: async ({ cookies, request, fetch }) => {},
};
