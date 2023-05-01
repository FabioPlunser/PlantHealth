import { BACKEND_URL } from "$env/static/private";

export async function load({ fetch }) {
  let res = await fetch(`${BACKEND_URL}/get-access-points`);
  res = await res.json();

  // console.log("access points", res);
  return {
    accessPoints: res?.accessPoints || [],
  };
}

export const actions = {
  unlock: async ({ cookies, request, fetch }) => {
    const formData = await request.formData();

    const res = await fetch(
      `${BACKEND_URL}/set-unlocked-access-point?accessPointId=${formData.get(
        "accessPointId"
      )}&unlocked=${formData.get("unlocked")}`,
      {
        method: "POST",
      }
    );
    const data = await res.json();
    return {
      status: res.status,
      body: JSON.stringify(data),
    };
  },
  delete: async ({ cookies, request, fetch }) => {
    console.log("delete");
  },
  search: async ({ cookies, request, fetch }) => {
    console.log("search");
  },
} satisfies Actions;
