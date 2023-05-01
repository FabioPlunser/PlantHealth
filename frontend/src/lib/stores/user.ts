import { writable } from "svelte/store";
import { browser } from "$app/env";
import type { User } from "./$types";

const Store = localStorage.getItem("token");
export const user = writable<User | null>(Store ? JSON.parse(Store) : null);
user.subscribe((value: User) => {
  if (browser) {
    if (value) localStorage.setItem("token", JSON.stringify(value));
    else localStorage.removeItem("token");
  }
});
