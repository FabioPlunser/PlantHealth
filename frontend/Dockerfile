# https://github.com/JustinyAhin/trigyte

FROM node:16-alpine
 
RUN npm install -g pnpm

WORKDIR /app
COPY package.json ./
RUN pnpm install 

COPY . ./
COPY ./.env ./.env
RUN pnpm build

EXPOSE 3000

ENV ORIGIN=http://localhost:3000
CMD ["node", "build"]
