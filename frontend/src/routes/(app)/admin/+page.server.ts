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
  setDates,
} from "$helper/sensorStation";

/**
 * Load function to fetch admint dashboard
 * @param event
 * @returns
 */
export async function load(event) {
  const { fetch } = event;
  let dates = setDates(event);

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
        if (!sensorStation.deleted) {
          sensorStation.data = getSensorStationData(
            event,
            sensorStation,
            dates
          );
          sensorStation.pictures = await getSensorStationPictures(
            event,
            sensorStation
          );
        } else {
          sensorStation.data = null;
          sensorStation.pictures = null;
        }
      }
      resolve({ sensorStations: dashBoardSensorStations });
    });
  }

  return {
    dates,
    numbers,
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
  addToDashboard,
  removeFromDashboard,
  updateFromTo,
  uploadPicture,
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
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  uploadPicture: async (event) => {
    await uploadPicture(event);
  },
};
