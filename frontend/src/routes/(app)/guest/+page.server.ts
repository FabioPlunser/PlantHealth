import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { redirect } from "@sveltejs/kit";

export const actions = {
  default: async ({ request, fetch }) => {
    const formData = await request.formData();
    throw redirect(
      303,
      "/guest/plant?sensorStationId=" + formData.get("sensorStationId")
    );
  },
} satisfies Actions;
