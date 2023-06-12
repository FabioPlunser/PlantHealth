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
} from "$helper/sensorStation";

export async function load(event) {
  const { cookies, fetch } = event;
  let cookieFrom = cookies.get("from") || "";
  let cookieTo = cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());
  // if cookies are set overwrite the dates
  if (cookieFrom !== "" || cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }
  let dates = {
    from: from,
    to: to,
  };
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

      let sensorStation: SensorStationDetailComponent = data.sensorStation;
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
  return {
    streamed: {
      sensorStation: getSensorStation(),
    },
    gardener,
    dates: {
      from,
      to,
    },
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
