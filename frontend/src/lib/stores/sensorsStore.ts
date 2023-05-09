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
];

export const sensorsStore = writable(sensors);
