import { createLogger, format, transports } from "winston";
const { combine, timestamp, printf } = format;
import { redirect } from "@sveltejs/kit";

const logFormat = printf(({ level, message, timestamp, payload }) => {
  const logMessage = payload
    ? `${message} ${JSON.stringify(payload)}`
    : message;
  return `${timestamp} [${level.toUpperCase()}]: ${logMessage}`;
});

/* This code is creating a logger object using the Winston library in TypeScript. The logger object has
a default logging level of "info" and is configured to output logs to the console and two log files
(one for all logs and one for errors). The log format includes a timestamp and a custom log message
format that includes the log level, message, and any additional payload data. The logger object is
then exported for use in other parts of the codebase. */
export const logger = createLogger({
  level: "info",
  format: combine(timestamp({ format: "YYYY-MM-DD HH:mm:ss" }), logFormat),
  transports: [
    new transports.Console(),
    new transports.File({
      filename: "logs/error.log",
      level: "error",
      maxsize: 5242880,
      maxFiles: 1,
    }),
    new transports.File({
      filename: "logs/all.log",
      maxsize: 5242880,
      maxFiles: 1,
    }),
  ],
});
