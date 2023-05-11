import { createLogger, format, transports } from "winston";
const { combine, timestamp, printf } = format;

const logFormat = printf(({ level, message, timestamp, payload }) => {
  const logMessage = payload
    ? `${message} ${JSON.stringify(payload)}`
    : message;
  return `${timestamp} [${level.toUpperCase()}]: ${logMessage}`;
});

export const logger = createLogger({
  level: "info",
  format: combine(timestamp({ format: "YYYY-MM-DD HH:mm:ss" }), logFormat),
  transports: [
    new transports.Console(),
    new transports.File({
      filename: "logs/error.log",
      level: "error",
      maxsize: 5242880,
      maxFiles: 5,
    }),
    new transports.File({
      filename: "logs/all.log",
      maxsize: 5242880,
      maxFiles: 5,
    }),
  ],
});
