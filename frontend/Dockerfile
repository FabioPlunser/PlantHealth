FROM node:lts-slim
 
RUN npm install -g pnpm

COPY . ./
ENV BACKEND_URL=http://spring:8080

RUN rm -rf node_modules
RUN rm -rf build

RUN pnpm install
RUN pnpm run build

ENTRYPOINT ["pnpm", "run", "docker"]