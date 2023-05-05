import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";

let from = new Date(Date.now() - 14 * 24 * 60 * 60 * 1000); // standard fetch of last 14 days;
let to = new Date();

export async function load({ locals, fetch }) {
  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  res = await res.json();

  let sensorStations = res?.sensorStations;

  for (let foundSensorStation of res?.sensorStations) {
    // console.log("foundSensorStation", foundSensorStation);
    foundSensorStation.picture = new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-newest-sensor-station-picture?sensorStationId=${foundSensorStation.sensorStationId}`
      );
      if (res.status !== 200) {
        foundSensorStation.picture = null;
        resolve(null);
      } else {
        res = await res.blob();
        let file = new File([res], "image", { type: res.type });
        let arrayBuffer = await res.arrayBuffer();
        let buffer = Buffer.from(arrayBuffer);
        let encodedImage =
          "data:image/" + res.type + ";base64," + buffer.toString("base64");
        resolve(encodedImage);
      }
    });
    sensorStations[sensorStations.indexOf(foundSensorStation)] =
      foundSensorStation;
  }

  let dashboard = await fetch(`${BACKEND_URL}/get-dashboard`);
  dashboard = await dashboard.json();
  dashboard = { sensorStations: [] };

  for (let sensorStation of dashboard?.sensorStations) {
    console.log("sensorStation", sensorStation);
    // let pictures = [];
    // for(let pictureId of sensorStation.pictureIds){
    //   console.log("pictureId", pictureId);
    //   let picture = new Promise(async (resolve, reject) => {
    //     let res = await fetch(`${BACKEND_URL}/get-sensor-station-picture?pictureId=${pictureId}`);
    //     if(res.status != 200){
    //       console.log("res", res);
    //       resolve(null);
    //     }
    //     res = await res.blob();
    //     let file = new File([res], "image", { type: res.type });
    //     let arrayBuffer = await res.arrayBuffer();
    //     let buffer = Buffer.from(arrayBuffer);
    //     let encodedImage =
    //       "data:image/" + res.type + ";base64," + buffer.toString("base64");
    //     let picture = {
    //       encodedImage: encodedImage,
    //       pictureId: pictureId,
    //     };
    //     resolve(picture);
    //   });
    //   pictures.push(picture);
    // }
    // sensorStation.pictures = pictures;

    sensorStation.data = new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
          sensorStation.sensorStationId
        }&from=${from.toISOString().split(".")[0]}&to=${
          to.toISOString().split(".")[0]
        }`
      );
      if (res.status != 200) {
        console.log("res", res);
        resolve(null);
      }
      res = await res.json();
      console.log(res);
      resolve(res);
    });

    dashboard.sensorStations[dashboard.sensorStations.indexOf(sensorStation)] =
      sensorStation;
  }

  // console.log("dashboard", dashboard);

  return {
    dashboard,
    sensorStations: sensorStations,
  };
}

export const actions = {
  addToDashboard: async ({ request, fetch, url }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");

    let res = await fetch(
      `${BACKEND_URL}/add-to-dashboard?sensorStationId=${sensorStationId}`,
      {
        method: "POST",
      }
    );
  },
  removeFromDashboard: async ({ request, fetch, url }) => {
    let formData = await request.formData();
    let sensorStationId = formData.get("sensorStationId");

    let res = await fetch(
      `${BACKEND_URL}/remove-from-dashboard?sensorStationId=${sensorStationId}`,
      {
        method: "DELETE",
      }
    );
    res = await res.json();
    console.log("removeFromDashboard", res);
  },

  updateFromTo: async ({ request, fetch, url }) => {
    let formData = await request.formData();
    from = formData.get("from");
    to = formData.get("to");

    console.log("from", from);
    console.log("to", to);
  },
} satisfies Actions;
