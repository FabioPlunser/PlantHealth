// import { app } from "$app/stores";
// import { get } from "svelte/store";

export async function logout() {
  let res = await fetch("/api/logout");
}
