import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "$helper/errorHandler";
import { error } from "@sveltejs/kit";

export async function load(event) {
  const { fetch, depends } = event;
  depends("app:getAccessPoints");

  async function getAccessPoints(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-access-points`)
        .then(async (res) => {
          if (!res.ok) {
            res = await res.json();
            errorHandler(
              event.locals.user?.personId,
              "Error while getting access points",
              res
            );
            reject(res);
          }
          let data = await res.json();
          resolve(data.accessPoints);
        })
        .catch((err) => {
          errorHandler(
            event.locals.user?.personId,
            "Error while getting access points",
            err
          );
          reject(err);
          throw error(500, "Error while getting access points");
        });
    });
  }

  return {
    streamed: {
      accessPoints: getAccessPoints(),
    },
  };
}

//-------------------------------------------------------------
//-------------------------------------------------------------
//-------------------------------------------------------------
//-------------------------------------------------------------
export const actions = {
  unlock: async (event) => {
    const { cookies, request, fetch } = event;
    const formData = await request.formData();

    let params = new URLSearchParams();
    params.append("accessPointId", String(formData.get("accessPointId")));
    params.append("unlocked", String(formData.get("unlocked")));

    await fetch(
      `${BACKEND_URL}/set-unlocked-access-point?${params.toString()}`,
      { method: "POST" }
    )
      .then(async (res: any) => {
        if (!res.ok) {
          res = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while unlocking access point",
            res
          );
        }
      })
      .catch((err: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while unlocking access point",
          err
        );
        throw error(500, "Error while unlocking access point");
      });
  },
  //---------------------------------------------
  //
  //---------------------------------------------
  update: async (event) => {
    const { cookies, request, fetch } = event;
    const formData = await request.formData();

    let params = new URLSearchParams();
    params.append("accessPointId", String(formData.get("accessPointId")));
    params.append("roomName", String(formData.get("roomName")));
    params.append("transferInterval", String(formData.get("transferInterval")));

    await fetch(`${BACKEND_URL}/update-access-point?${params.toString()}`, {
      method: "POST",
    })
      .then(async (res: any) => {
        if (!res.ok) {
          res = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while updating access point",
            res
          );
        }
      })
      .catch((err: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while updating access point",
          err
        );
        throw error(500, "Error while updating access point");
      });
  },
  //---------------------------------------------
  //
  //---------------------------------------------
  scan: async (event) => {
    const { cookies, request, fetch } = event;
    const formData = await request.formData();
    let accessPointId = formData.get("accessPointId");

    await fetch(
      `${BACKEND_URL}/scan-for-sensor-stations?accessPointId=${accessPointId}`,
      { method: "POST" }
    )
      .then(async (res: any) => {
        if (!res.ok) {
          res = await res.json();
          errorHandler(
            event.locals.user?.personId,
            "Error while scanning for sensor stations",
            res
          );
        }
      })
      .catch((err: any) => {
        errorHandler(
          event.locals.user?.personId,
          "Error while scanning for sensor stations",
          err
        );
        throw error(500, "Error while scanning for access point");
      });
  },

  delete: async (event) => {
    const { request, fetch } = event;
  let formData = await request.formData();
  let accessPointId: string = String(formData.get("accessPointId"));

  let params = new URLSearchParams();
  params.set("accessPointId", accessPointId);

  await fetch(`${BACKEND_URL}/delete-access-point?${params.toString()}`, {
    method: "DELETE",
  })
    .then((res: any) => {
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Error while deleting access point",
          res
        );
      } else {
        logger.info(`Deleted access point = ${accessPointId}`);
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Deleted access point"
        );
      }
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while deleting access point",
        e
      );
    });
  },
};
