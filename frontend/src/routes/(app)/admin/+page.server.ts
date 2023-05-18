import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect, error } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "$lib/helper/errorHandler";

import {
  getAllSensorStations,
  getSensorStationData,
  getSensorStationPictures,
} from "$helper/sensorStation";

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
        errorHandler(
          String(event.locals.user?.personId),
          "Error while fetching dashboard sensor stations",
          await res.json()
        );
        throw error(res.status, {
          message: "Error while fetching dashboard sensor stations",
        });
      }
      let data = await res.json();
      return {
        users: data?.numOfUsers,
        accessPoints: data?.numOfConnectedAccessPoints,
        sensorStations: data?.numOfConnectedSensorStations,
      };
    })
    .catch((err) => {
      errorHandler(
        String(event.locals.user?.personId),
        "Error while fetching dashboard sensor stations",
        err
      );
      throw error(500, {
        message: "Error while fetching dashboard sensor stations",
      });
    });

  async function getDashBoardSensorStations(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      let sensorStations = await fetch(`${BACKEND_URL}/get-dashboard`).then(
        async (res) => {
          if (!res.ok) {
            errorHandler(
              String(event.locals.user?.personId),
              "Error while fetching dashboard sensor stations",
              await res.json()
            );
            reject(null);
            throw error(res.status, {
              message: "Error while fetching dashboard sensor stations",
            });
          }
          let data = await res.json();
          return data?.sensorStations;
        }
      );

      for (let sensorStation of sensorStations) {
        sensorStation.data = getSensorStationData(event, sensorStation, dates);
        sensorStation.pictures = await getSensorStationPictures(
          event,
          sensorStation
        );
      }
      resolve(sensorStations);
    }).catch((err) => {
      errorHandler(
        String(event.locals.user?.personId),
        "Error while fetching dashboard sensor stations",
        err
      );
      throw error(500, {
        message: "Error while fetching dashboard sensor stations",
      });
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
import {
  addToDashboard,
  removeFromDashboard,
  updateFromTo,
} from "$helper/actions";

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
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  updateFromTo: async (event) => {
    await updateFromTo(event);
  },
};
