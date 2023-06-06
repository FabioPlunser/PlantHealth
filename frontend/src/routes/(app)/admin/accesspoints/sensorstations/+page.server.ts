import { BACKEND_URL } from "$env/static/private";
import { error, fail, redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";

export async function load(event) {
  const { fetch, request, url, depends } = event;

  let accessPointId = url.searchParams.get("accessPointId");
  async function getSensorStations(): Promise<Responses.AdminSensorStationsResponse> {
    return new Promise(async (resolve) => {
      await fetch(
        `${BACKEND_URL}/get-access-point-sensor-stations?accessPointId=${accessPointId}`
      )
        .then(async (res: any) => {
          if (!res.ok) {
            logger.error("Could not get all sensor stations");
          }
          let data = await res.json();
          resolve(data);
        })
        .catch((err) => {
          logger.error("Catch Could not get all sensor stations", {
            payload: err,
          });
          resolve({ sensorStations: [] });
        });
    });
  }
  async function getGardener(): Promise<Responses.ListResponse> {
    return new Promise(async (resolve, reject) => {
      await fetch(`${BACKEND_URL}/get-all-gardener`)
        .then(async (res) => {
          if (!res.ok) {
            logger.error("Could not get gardener");
          }
          resolve(await res.json());
        })
        .catch((err) => {
          logger.error("Could not get gardener");
          resolve({ items: [] });
        });
    });
  }

  return {
    gardener: getGardener(),
    streamed: {
      sensorStations: getSensorStations(),
    },
  };
}

//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
import {
  deleteSensorStation,
  updateSensorStation,
  unlockSensorStation,
  assignGardener,
} from "$helper/actions";

export const actions = {
  unlock: async (event) => {
    await unlockSensorStation(event);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  update: async (event) => {
    let formData = await event.request.formData();
    await updateSensorStation(event, formData);
    await assignGardener(event, formData);
  },
  //---------------------------------------------------------------------
  //
  //---------------------------------------------------------------------
  delete: async ({ request, fetch, locals }) => {
    await deleteSensorStation({ request, fetch, locals });
  },
};
