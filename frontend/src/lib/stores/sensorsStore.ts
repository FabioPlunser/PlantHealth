import { writable } from "svelte/store";

let sensors = [
  {
    sensorType: "TEMPERATURE",
    sensorUnit: "°C",
    bootstrap: "bi-thermometer-half",
    google: "",
  },
  {
    sensorType: "HUMIDITY",
    sensorUnit: "%",
    bootstrap: "bi-droplet-half",
    google: "",
  },
  {
    sensorType: "LIGHTINTENSITY",
    sensorUnit: "lx",
    bootstrap: "bi-sun",
    google: "",
  },
  {
    sensorType: "PRESSURE",
    sensorUnit: "hPa",
    bootstrap: "",
    google: "speed",
  },
  {
    sensorType: "BATTERYLEVEL",
    sensorUnit: "%",
    bootstrap: "bi-battery-half",
    google: "",
  },
  {
    sensorType: "SOILHUMIDITY",
    sensorUnit: "%",
    bootstrap: "bi bi-moisture",
    google: "",
  },
  {
    sensorType: "AIRQUALITY",
    sensorUnit: "%",
    bootstrap: "",
    google: "air",
  },
];

export const sensorsStore = writable(sensors);
