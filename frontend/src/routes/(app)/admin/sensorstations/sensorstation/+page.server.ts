import { BACKEND_URL } from "$env/static/private";
import { logger } from "$helper/logger";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";
import { toasts } from "$stores/toastStore";

import { errorHandler } from "$helper/errorHandler";

import {
  getAllSensorStations,
  getSensorStationData,
  getSensorStationPictures,
  getSensorStationLimits,
  setDates,
} from "$helper/sensorStation";

export async function load(event) {
  const { cookies, fetch } = event;

  //-------------------------------------------------------------------------------------------------------------------------
  // get sensor station
  //-------------------------------------------------------------------------------------------------------------------------
  let sensorStationId = String(cookies.get("sensorStationId"));

  async function getSensorStation(): Promise<SensorStationDetailComponent> {
    return new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-sensor-station?sensorStationId=${sensorStationId}`
      );
      if (!res.ok) {
        errorHandler(
          event.locals.user?.personId,
          "Couldn't get sensor station",
          res
        );
      }

      let data: Responses.SensorStationDetailResponse = await res.json();

      let sensorStation: SensorStationDetailComponentInner = data.sensorStation;
      sensorStation.data = getSensorStationData(event, sensorStation, dates);
      sensorStation.pictures = await getSensorStationPictures(
        event,
        sensorStation
      );

      resolve(sensorStation);
    });
  }
  //-------------------------------------------------------------------------------------------------------------------------
  // get all gardener
  //-------------------------------------------------------------------------------------------------------------------------
  let gardener = null;
  let res = await fetch(`${BACKEND_URL}/get-all-gardener`);
  if (!res.ok) {
    logger.error("Could not get gardener");
    throw error(res.status, "Could not get gardener");
  } else {
    gardener = await res.json();
  }
  //-------------------------------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------------------------------
  let dates = setDates(event);
  return {
    streamed: {
      sensorStation: getSensorStation(),
    },
    gardener,
    dates,
  };
}

//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
import {
  uploadPicture,
  deleteAllPictures,
  deletePicture,
  updateFromTo,
  deleteSensorStation,
  updateLimit,
  updateSensorStation,
  unlockSensorStation,
  assignGardener,
} from "$helper/actions";

export const actions = {
  unlock: async (event) => {
    unlockSensorStation(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
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
  delete: async (event) => {
    await deleteSensorStation(event);
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
};
