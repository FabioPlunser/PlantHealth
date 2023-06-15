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
  setDates,
} from "$helper/sensorStation";

export async function load(event) {
  const { cookies, fetch } = event;
  let dates = setDates(event);
  //---------------------------------------------------------------------
  // get dashboard, assigned and added to dashboard sensor stations
  //---------------------------------------------------------------------
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

  let dashBoardSensorStations: SensorStationComponent[] =
    data.addedSensorStations;
  let assignedSensorStations: SensorStationDetailComponentInner[] =
    data.assignedSensorStations;

  async function getDashBoardSensorStations(): Promise<Dashboard> {
    return new Promise(async (resolve, reject) => {
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

  async function getAssignedSensorStations(): Promise<
    SensorStationDetailComponentInner[] | []
  > {
    return new Promise(async (resolve, reject) => {
      if (assignedSensorStations.length == 0) resolve([]);
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
      }
      resolve(assignedSensorStations);
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
