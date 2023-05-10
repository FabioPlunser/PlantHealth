import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";

// TODO: add validation and error handling (toast messages)
export async function load({ fetch, depends }) {
  let res = await fetch(`${BACKEND_URL}/get-access-points`);
  if (!res.ok) {
    logger.error("Could not get access points");
    throw new error(res.status, "Could not get access points");
  }
  res = await res.json();
  logger.info("Got access points");
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
    if (!res.ok) {
      logger.error("Could not unlock access point");
      throw new error(res.status, "Could not unlock access point");
    } else {
      logger.info("Unlocked access point");
    }
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
    if (!res.ok) {
      logger.error("Could not update access point");
      throw new error(res.status, "Could not update access point");
    } else {
      logger.info("Updated access point");
    }
  },

  // TODO: add validation and error handling (toast messages)
  scan: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();
    let accessPointId = formData.get("accessPointId");

    let res = await fetch(
      `${BACKEND_URL}/scan-for-sensor-stations?accessPointId=${accessPointId}`,
      { method: "POST" }
    );
    if (!res.ok) {
      logger.error("Could not scan for sensor stations");
      throw new error(res.status, "Could not scan for sensor stations");
    } else {
      logger.info("Scanned for sensor stations");
    }
  },

  delete: async ({ cookies, request, fetch }) => {},
} satisfies Actions;
