import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch }) {
  let res = await fetch(`http://${BACKEND_URL}/get-access-points`);
  res = await res.json();

  console.log("access points", res);
  return { undefined };
}
