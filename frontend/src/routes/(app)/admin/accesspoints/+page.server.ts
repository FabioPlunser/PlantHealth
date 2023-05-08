import { BACKEND_URL } from "$env/static/private";

// TODO: add validation and error handling (toast messages)
export async function load({ fetch, depends }) {
  let res = await fetch(`${BACKEND_URL}/get-access-points`);
  res = await res.json();
  console.log("accessPoints", res.accessPoints);
  depends("app:getAccessPoints");
  return {
    accessPoints: res.accessPoints,
  };
}

export const actions = {
  // TODO: add validation and error handling (toast messages)
  unlock: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();

    const res = await fetch(
      `${BACKEND_URL}/set-unlocked-access-point?accessPointId=${formData.get(
        "accessPointId"
      )}&unlocked=${formData.get("unlocked")}`,
      {
        method: "POST",
      }
    );
  },

  // TODO: add validation and error handling (toast messages)
  update: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    let accessPointId = formData.get("accessPointId");
    let roomName = formData.get("roomName");
    let transferInterval = formData.get("transferInterval");

    let res = await fetch(
      `${BACKEND_URL}/update-access-point?accessPointId=${accessPointId}
      &roomName=${roomName}&transferInterval=${transferInterval}`,
      { method: "POST" }
    );
  },

  // TODO: add validation and error handling (toast messages)
  scan: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    let accessPointId = formData.get("accessPointId");
    console.log("scan", accessPointId);

    let res = await fetch(
      `${BACKEND_URL}/scan-for-sensor-stations?accessPointId=${accessPointId}`,
      { method: "POST" }
    );
    res = await res.json();
    console.log("scan", res);
  },

  delete: async ({ cookies, request, fetch }) => {
    console.log("delete");
  },
} satisfies Actions;
