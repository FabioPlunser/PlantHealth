import { BACKEND_URL } from "$env/static/private";
import { error, fail, redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";

export async function load(event) {
  const { fetch, request, url, depends } = event;
  depends("app:getSensorStations");

  async function getAllSensorStations(): Promise<Responses.AdminSensorStationsResponse> {
    return new Promise(async (resolve) => {
      await fetch(`${BACKEND_URL}/get-all-sensor-stations`)
        .then(async (res: any) => {
          if (!res.ok) {
            logger.error("Could not get all sensor stations");
          }
          let data = await res.json();
          /* HACK: we have to set a default value for our gardener.personId
           * to be able to communicate the selected id from the GardenerSelect.svelte component
           */
          data.sensorStations.forEach((station: any) => {
            try {
              if (!station.gardener) {
                Object.defineProperty(station, "gardener", {
                  value: { personId: "No gardener assigned" },
                  writable: true,
                  enumerable: true,
                  configurable: true,
                });
              }
            } catch (e) {
              console.log(e);
            }
          });

          resolve(data.sensorStations);
        })
        .catch((err) => {
          logger.error("Catch Could not get all sensor stations", {
            payload: err,
          });
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
        });
    });
  }

  return {
    gardener: getGardener(),
    streamed: {
      sensorStations: getAllSensorStations(),
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
