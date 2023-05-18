import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect, error } from "@sveltejs/kit";

import { errorHandler } from "$helper/errorHandler";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";

import {
  getAllSensorStations,
  getSensorStationData,
  getSensorStationPictures,
  getSensorStationLimits,
} from "$helper/sensorStation";

export async function load(event) {
  const { cookies, fetch } = event;

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

  let data = await fetch(`${BACKEND_URL}/get-dashboard`).then(async (res) => {
    if (!res.ok) {
      errorHandler(
        event.locals.user?.personId,
        "Error while fetching dashboard sensor stations",
        await res.json()
      );
    }
    return await res.json();
  });

  let dashBoardSensorStations = data?.sensorStations;

  async function getDashBoardSensorStations(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      if (dashBoardSensorStations.length == 0) resolve([]);
      for (let sensorStation of dashBoardSensorStations) {
        sensorStation.data = getSensorStationData(event, sensorStation, dates);
        sensorStation.pictures = await getSensorStationPictures(
          event,
          sensorStation
        );
      }

      resolve(dashBoardSensorStations);
    }).catch((e) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while fetching dashboard sensor stations",
        e
      );
      return null;
    });
  }

  return {
    dates,
    streamed: {
      allSensorStations: getAllSensorStations(event),
      dashBoardSensorStations: getDashBoardSensorStations(),
    },
  };
}

//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
import {
  updateFromTo,
  addToDashboard,
  removeFromDashboard,
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
