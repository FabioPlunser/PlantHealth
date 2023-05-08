import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch }) {
  let res = await fetch(`${BACKEND_URL}/get-access-points`);
  res = await res.json();

  return { undefined };
}
