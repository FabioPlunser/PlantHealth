import { BACKEND_URL } from "$env/static/private";
import type { Actions } from "./$types";
import { redirect } from "@sveltejs/kit";
import { logger } from "$helper/logger";
import { toasts } from "$stores/toastStore";
import fs from "fs";
import path from "path";

export async function load({ fetch, locals }) {
  let numbers = await fetch(`${BACKEND_URL}/get-dashboard`);
  numbers = await numbers.json();

  let backendLogs = await fetch(`${BACKEND_URL}/get-logs`);
  backendLogs = await backendLogs.json();
  backendLogs = backendLogs.logs;
  backendLogs.forEach((element) => {
    element.timestamp = new Date(element.timestamp).toLocaleString("de-DE");
  });

  let allLog = new Promise(async (resolve, reject) => {
    const filePath = path.resolve("./logs/all.log");
    var logFile = await fs.readFileSync(filePath, { encoding: "utf-8" });
    const lines = logFile.split("\n");
    const logs = [];
    lines.forEach((line) => {
      if (line) {
        // skip empty lines
        const logRegex =
          /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) \[(\w+)\]: (.*)$/;
        const [, timeStamp, severity, message] = line.match(logRegex);
        const logObject = { timeStamp, severity, message };
        logObject.timeStamp = new Date(logObject.timeStamp).toLocaleString(
          "de-DE"
        );
        logObject.message = logObject.message.slice(0, 100);
        logs.push(logObject);
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
