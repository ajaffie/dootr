FROM node:12-alpine AS build
WORKDIR /app
COPY package.json /app/
RUN npm install
COPY . /app/
RUN npm run build

FROM nginx:1.17.4-alpine AS final
COPY --from=build /app/build/ /usr/share/nginx/html/app