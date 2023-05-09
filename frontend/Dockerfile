# https://github.com/JustinyAhin/trigyte

FROM node:16-alpine
 
RUN npm install -g pnpm

WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install 

COPY . ./
COPY ./.env ./.env

ENV BACKEND_URL http://spring:8433
RUN pnpm build

EXPOSE 3000

ENV ORIGIN http://localhost:3000
CMD ["node", "build"]
