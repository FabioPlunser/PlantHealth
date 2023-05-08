import { writable } from "svelte/store";

let sensors = [
  {
    type: "TEMPERATURE",
    icon: "bi-thermometer-half",
    google: "",
  },
  {
    type: "HUMIDITY",
    icon: "bi-droplet-half",
    google: "",
  },
  {
    type: "LIGHTINTENSITY",
    icon: "bi-sun",
    google: "",
  },
  {
    type: "PRESSURE",
    icon: "",
    google: "speed",
  },
  {
    type: "GASPRESSURE",
    icon: "",
    google: "nest_thermostat_zirconium_eu",
  },
  {
    type: "SOILHUMIDITY",
    icon: "bi-moisture",
    google: "",
  },
];

export const sensorsStore = writable(sensors);
