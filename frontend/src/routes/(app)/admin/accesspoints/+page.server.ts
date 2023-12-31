import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "$helper/errorHandler";
import { error } from "@sveltejs/kit";
import { updateFromTo } from "$helper/actions";

export async function load(event) {
  const { fetch, depends } = event;
  depends("app:getAccessPoints");

  async function getAccessPoints(): Promise<Responses.AccessPointListResponse | null> {
    return new Promise(async (resolve, reject) => {
      let res = await fetch(`${BACKEND_URL}/get-access-points`)
        .then(async (res) => {
          if (!res.ok) {
            res = await res.json();
            errorHandler(
              event.locals.user?.personId,
              "Error while getting access points",
              res
            );
            throw error(500, "Error while getting access points");
            resolve(null);
          }
          resolve(await res.json());
        })
        .catch((err) => {
          errorHandler(
            event.locals.user?.personId,
            "Error while getting access points",
            err
          );
          resolve(null);
        });
    });
  }

  return {
    streamed: {
      accessPoints: getAccessPoints().catch((err) => {
        throw error(500, "Error while getting access points");
      }),
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
    let accessPointId = String(formData.get("accessPointId"));
    let scanActive = String(formData.get("scanActive"));
    await fetch(
      `${BACKEND_URL}/scan-for-sensor-stations?accessPointId=${accessPointId}&scanActive=${scanActive}`,
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
        toasts.addToast(
          event.locals.user?.personId,
          "success",
          "Set access point scanning to: " + scanActive
        );
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
  updateFromTo: async (event) => {
    await updateFromTo(event);
  },
};
