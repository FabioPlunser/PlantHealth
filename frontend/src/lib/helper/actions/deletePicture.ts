import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { ErrorHandler } from "../errorHandler";
import { error } from "@sveltejs/kit";

export async function deletePicture(event: any) {
  const { request, fetch } = event;
  let formData = await request.formData();
  let pictureId = String(formData.get("pictureId"));
  let params = new URLSearchParams();
  params.set("pictureId", pictureId);

  let requestOptions = {
    method: "POST",
  };

  await fetch(
    `${BACKEND_URL}/delete-sensor-station-picture?${params.toString()}`,
    requestOptions
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        ErrorHandler(
          event.locals.user?.personId,
          "Error while deleting picture",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Picture deleted"
      );
    })
    .catch((e) => {
      ErrorHandler(
        event.locals.user?.personId,
        "Error while deleting picture",
        e
      );
      throw error(500, "Error while deleting picture");
    });
}
