import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";

// let from = new Date(Date.now() - 24 * 60 * 60 * 1000); // standard fetch of last 14 days;
// let to = new Date(Date.now());

export async function load({ locals, fetch, cookies }) {
  let from = cookies.get("from");
  let to = cookies.get("to");

  console.log("Load-from-cookies", from);
  console.log("Load-to-cookies", to);

  if (!from || !to) {
    from = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    to = new Date(Date.now());
    cookies.set("from", from, { path: "/" });
    cookies.set("to", to, { path: "/" });
  } else {
    from = new Date(from);
    to = new Date(to);
  }

  console.log("load-from", from);
  console.log("load-to", to);

  let res = await fetch(`${BACKEND_URL}/get-sensor-stations`);
  res = await res.json();

  let sensorStations = res?.sensorStations;
  for (let foundSensorStation of res?.sensorStations) {
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

  for (let sensorStation of dashboard.sensorStations) {
    // console.log("dashboard", sensorStation);
    sensorStation.data = new Promise(async (resolve, reject) => {
      let res = await fetch(
        `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
          sensorStation.sensorStationId
        }&from=${from.toISOString().split(".")[0]}&to=${
          to.toISOString().split(".")[0]
        }`
      );
      if (res.status != 200) {
        // console.log("res", res);
        resolve(null);
      }
      res = await res.json();
      // console.log("sensorStationData", res);
      resolve(res);
    });
  }
  // sensorStation.data = new Promise(async (resolve, reject) => {
  //   let res = await fetch(
  //     `${BACKEND_URL}/get-sensor-station-data?sensorStationId=${
  //       sensorStation.sensorStationId
  //     }&from=${from.toISOString().split(".")[0]}&to=${
  //       to.toISOString().split(".")[0]
  //     }`
  //   );
  //   if (res.status != 200) {
  //     console.log("res", res);
  //     resolve(null);
  //   }
  //   res = await res.json();
  //   console.log(res);
  //   resolve(res);
  // });

  // dashboard.sensorStations[dashboard.sensorStations.indexOf(sensorStation)] =
  //   sensorStation;

  // console.log("dashboard", dashboard);

  return {
    dashboard,
    dates: {
      from,
      to,
    },
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
    // console.log("removeFromDashboard", res);
  },

  updateFromTo: async ({ request, fetch, url, cookies }) => {
    let formData = await request.formData();
    let _from = formData.get("from");
    let _to = formData.get("to");

    console.log("UpdateFromTo");
    console.log("from", _from);
    console.log("to", _to);

    _from = new Date(_from);
    _to = new Date(_to);

    console.log("from", _from);
    console.log("to", _to);

    cookies.set("from", _from, { path: "/" });
    cookies.set("to", _to, { path: "/" });

    console.log("Cookies-from", new Date(cookies.get("from")));
    console.log("Cookies-to", new Date(cookies.get("to")));
  },
} satisfies Actions;
