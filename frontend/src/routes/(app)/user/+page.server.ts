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

  async function getDashBoardSensorStations(): Promise<Responses.UserDashBoardResponse> {
    return new Promise(async (resolve, reject) => {
      let res = await fetch(`${BACKEND_URL}/get-dashboard`);
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching dashboard sensor stations",
          await res.json()
        );
      }
      let data = await res.json();
      console.log(data);
      let dashBoardSensorStations = data.sensorStations;
      if (dashBoardSensorStations.length == 0) resolve({ sensorStations: [] });
      for (let sensorStation of dashBoardSensorStations) {
        sensorStation.data = getSensorStationData(event, sensorStation, dates);
        sensorStation.pictures = await getSensorStationPictures(
          event,
          sensorStation
        );
      }

      resolve({ sensorStations: dashBoardSensorStations });
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
