import { type Writable, writable } from "svelte/store";

export const apSensorStations: Writable<
  Responses.AdminSensorStationsResponse | any
> = writable([]);
