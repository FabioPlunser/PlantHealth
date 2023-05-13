import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

interface Dashboard {
  sensorStations: SensorStation[];
}

export async function load({ cookies, fetch }) {
  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());

  if (cookieFrom !== "" && cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }

  let res = await fetch(`${BACKEND_URL}/get-dashboard`);
  if (!res.ok) {
    logger.error(
      `Error while fetching dashboard data: ${res.status} ${res.statusText}`
    );
    throw error(res.status, "Error while fetching dashboard data");
  }

  let dashboardData = await res.json();
  console.log(dashboardData);
  return {
    dashboardData,
    dates: {
      from,
      to,
    },
  };
}

// export const actions = {
//   updateSensorStation: async (e) => {
//   },
//   deletePicture: async (e) => {
//   },
// }
