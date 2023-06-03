import { BACKEND_URL } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

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

  //---------------------------------------------------------------------
  // get all sensor stations available to add to dashboard
  //---------------------------------------------------------------------
  let data = await fetch(`${BACKEND_URL}/get-dashboard`)
    .then(async (res) => {
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Error while fetching dashboard sensor stations",
          await res.json()
        );
      }
      return await res.json();
    })
    .catch((e) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while fetching dashboard sensor stations",
        e
      );
      throw error(500, "Error while fetching dashboard sensor stations");
    });

  let dashBoardSensorStations = data?.addedSensorStations;
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

  let assignedSensorStations = data?.assignedSensorStations;
  async function getAssignedSensorStations(): Promise<any> {
    return new Promise(async (resolve, reject) => {
      if (assignedSensorStations.length == 0)
        for (let assignedSensorStation of assignedSensorStations) {
          assignedSensorStation.data = getSensorStationData(
            event,
            assignedSensorStation,
            dates
          );
          assignedSensorStation.pictures = await getSensorStationPictures(
            event,
            assignedSensorStation
          );
          assignedSensorStation.limits = getSensorStationLimits(
            event,
            assignedSensorStation
          );
        }
      resolve(assignedSensorStations);
    }).catch((e) => {
      errorHandler(
        event.locals.user?.personId ?? "unknown",
        "Error while fetching dashboard sensor stations",
        e
      );
    });
  }

  return {
    dates,
    streamed: {
      allSensorStations: getAllSensorStations(event),
      dashBoardSensorStations: getDashBoardSensorStations(),
      assignedSensorStations: getAssignedSensorStations(),
    },
  };
}
//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
//---------------------------------------------------------------------
import {
  uploadPicture,
  deleteAllPictures,
  deletePicture,
  updateFromTo,
  deleteSensorStation,
  updateLimit,
  updateSensorStation,
  addToDashboard,
  removeFromDashboard,
} from "$helper/actions";

export const actions = {
  updateSensorStation: async (event) => {
    await updateSensorStation(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  updateLimit: async (event) => {
    await updateLimit(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  deletePicture: async (event) => {
    await deletePicture(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  deleteAllPictures: async (event) => {
    await deleteAllPictures(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  uploadPicture: async (event) => {
    await uploadPicture(event);
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
