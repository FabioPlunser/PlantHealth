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
  setDates,
} from "$helper/sensorStation";

export async function load(event) {
  const { cookies, fetch } = event;

  async function getDashBoardSensorStations(): Promise<Dashboard> {
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

  let dates = setDates(event);
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
