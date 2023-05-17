import { writable } from "svelte/store";

let sensors = [
  {
    sensorType: "TEMPERATURE",
    sensorUnit: "°C",
    bootstrap: "bi-thermometer-half",
    google: "",
  },
  {
    sensorType: "Air Humidity",
    sensorUnit: "%",
    bootstrap: "bi-droplet-half",
    google: "",
  },
  {
    sensorType: "Light Intensity",
    sensorUnit: "lx",
    bootstrap: "bi-sun",
    google: "",
  },
  {
    sensorType: "Air Pressure",
    sensorUnit: "hPa",
    bootstrap: "",
    google: "speed",
  },
  {
    sensorType: "Battery Level",
    sensorUnit: "%",
    bootstrap: "bi-battery-half",
    google: "",
  },
  {
    sensorType: "Earth Humidity",
    sensorUnit: "%",
    bootstrap: "bi bi-moisture",
    google: "",
  },
  {
    sensorType: "Air Quality",
    sensorUnit: "%",
    bootstrap: "",
    google: "air",
  },
];

export const sensorsStore = writable(sensors);
