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

  async function getFrontendLogs() {
    const filePath = path.resolve("./logs/all.log");
    const logs: any[] = [];

    const readableStream = fs.createReadStream(filePath, { encoding: "utf-8" });

    return new Promise((resolve, reject) => {
      readableStream.on("data", (chunk) => {
        const lines = chunk.split("\n");
        lines.forEach((line: any) => {
          if (line) {
            // skip empty lines
            const logRegex =
              /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) \[(\w+)\]: (.*)$/;
            const match = line.match(logRegex);
            if (match) {
              const [, timeStamp, severity, message] = match;
              const logObject = { timeStamp, severity, message };
              logObject.timeStamp = new Date(
                logObject.timeStamp
              ).toLocaleString("de-DE");
              logObject.message = logObject.message.slice(0, 100);
              logs.push(logObject);
            }
          }
        });
      });

      readableStream.on("end", () => {
        resolve(logs);
      });

      readableStream.on("error", (error) => {
        reject(error);
      });
    });
  }

  return {
    streamed: {
      backendLogs: getBackendLogs(),
      frontendLogs: getFrontendLogs(),
    },
  };
}
