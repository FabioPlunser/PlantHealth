import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import fs from "fs";
import path from "path";

/**
 * Load function to fetch admint dashboard
 * @param event
 * @returns
 */
export async function load({ fetch }) {
  let res = await fetch(`${BACKEND_URL}/get-dashboard`);
  let numbers = await res.json();

  res = await fetch(`${BACKEND_URL}/get-logs`);
  let backendLogs = await res.json();
  backendLogs = backendLogs.logs;

  let allLog = new Promise(async (resolve, reject) => {
    const filePath = path.resolve("./logs/all.log");
    var logFile = await fs.readFileSync(filePath, { encoding: "utf-8" });
    const lines = logFile.split("\n");
    const logs: any[] = [];
    lines.forEach((line) => {
      if (line) {
        // skip empty lines
        const logRegex =
          /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) \[(\w+)\]: (.*)$/;
        const match = line.match(logRegex);
        if (match) {
          const [timeStamp, severity, message] = match;
          const logObject = { timeStamp, severity, message };
          logObject.timeStamp = new Date(logObject.timeStamp).toLocaleString(
            "de-DE"
          );
          logObject.message = logObject.message.slice(0, 100);
          logs.push(logObject);
        }
      }
    });
    resolve(logs);
  });
  return {
    numbers,
    backend: backendLogs,
    streamed: {
      frontend: allLog,
    },
  };
}
