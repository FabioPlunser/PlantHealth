import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import { ErrorHandler } from "$lib/helper/errorHandler";

import {
  getAllSensorStations,
  getSensorStationData,
  getSensorStationPictures,
} from "$helper/SensorStation";

/**
 * Load function to fetch admint dashboard
 * @param event
 * @returns
 */
export async function load(event) {
  const { cookies, fetch } = event;

  //---------------------------------------------------------------------
  /**
   * Check if any date is set if not set default to last 7 days
   */
  //---------------------------------------------------------------------
  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());

  if (cookieFrom !== "" && cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }

  let dates = {
    from: from,
    to: to,
  };
  //---------------------------------------------------------------------
  // get all sensor stations in dashboard
  //---------------------------------------------------------------------
  let numbers = await fetch(`${BACKEND_URL}/get-dashboard`)
    .then(async (res) => {
      if (!res.ok) {
        ErrorHandler(
          String(event.locals.user?.personId),
          "Error while fetching dashboard sensor stations",
          await res.json()
        );
        return await res.json();
      }
      let data = await res.json();
      return {
        users: data?.numOfUsers,
        accessPoints: data?.numOfConnectedAccessPoints,
        sensorStations: data?.numOfConnectedSensorStations,
      };
    })
    .catch((err) => {
      ErrorHandler(
        String(event.locals.user?.personId),
        "Error while fetching dashboard sensor stations",
        err
      );
    });

  async function getDashBoardSensorStations(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      let sensorStations = await fetch(`${BACKEND_URL}/get-dashboard`).then(
        async (res) => {
          if (!res.ok) {
            ErrorHandler(
              String(event.locals.user?.personId),
              "Error while fetching dashboard sensor stations",
              await res.json()
            );
            return await res.json();
          }
          let data = await res.json();
          return data?.sensorStations;
        }
      );

      for (let sensorStation of sensorStations) {
        sensorStation.data = getSensorStationData(event, sensorStation, dates);
        sensorStation.pictures = getSensorStationPictures(event, sensorStation);
      }
      resolve(sensorStations);
    });
  }

  return {
    dates,
    numbers,
    streamed: {
      allSensorStations: getAllSensorStations(event),
      dashboardSensorStations: getDashBoardSensorStations(),
    },
  };
}

//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
import { addToDashboard, removeFromDashboard } from "$helper/actions";

export const actions = {
  addToDashboard: async (event) => {
    await addToDashboard(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  removeFromDashboard: async (event) => {
    await removeFromDashboard(event);
  },
};
