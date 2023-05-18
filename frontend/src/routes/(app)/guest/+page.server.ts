import type { Actions } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { redirect } from "@sveltejs/kit";

export const actions = {
  /* This is defining an action called `default` that is an asynchronous function. It takes in an
  object with two properties, `request` and `fetch`. It then uses the `request` object to get form
  data and constructs a redirect URL with a query parameter based on the form data. Finally, it
  throws a `redirect` error with a status code of 303 and the constructed URL as the target. This
  action is used in a SvelteKit application to handle form submissions and redirect the user to a
  new page with the appropriate query parameter. */
  default: async ({ request, fetch }) => {
    const formData = await request.formData();
    throw redirect(
      307,
      "/guest/plant?sensorStationId=" + formData.get("sensorStationId")
    );
  },
} satisfies Actions;
