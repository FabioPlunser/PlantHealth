import { BACKEND_URL } from "$env/static/private";

import fs from "fs";
import path from "path";
import { Readable } from "stream";

export async function load({ fetch }) {
  async function getBackendLogs() {
    return await fetch(`${BACKEND_URL}/get-logs`)
      .then(async (res: any) => {
        if (!res.ok) {
          return null;
        }
        return res.json();
      })
      .then((json) => {
        return json.logs;
      });
  }

  return {
    streamed: {
      backendLogs: getBackendLogs(),
    },
  };
}
