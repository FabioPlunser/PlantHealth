# https://github.com/JustinyAhin/trigyte

FROM node:16-alpine

RUN npm install -g pnpm

WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install 

COPY . ./
ENV BACKEND_URL=http://spring:8080
RUN pnpm build

EXPOSE 3000
ENV BACKEND_URL=http://spring:8080
ENV ORIGIN=http://localhost:3000
CMD ["node", "build/index.js"]
