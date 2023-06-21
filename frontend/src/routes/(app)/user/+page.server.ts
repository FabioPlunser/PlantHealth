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
  let dates = setDates(event);

  let data = await fetch(`${BACKEND_URL}/get-dashboard`)
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
      return await res.json();
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
          sensorStation.data = [];
          sensorStation.pictures = [];
        }
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
