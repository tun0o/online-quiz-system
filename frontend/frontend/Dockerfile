# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Stage 2: Serve the static files with Nginx
FROM nginx:1.25-alpine

# Set timezone to Vietnam
ENV TZ=Asia/Ho_Chi_Minh
RUN apk update && apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo "$TZ" > /etc/timezone \
    && apk del tzdata

# Remove default config & add your custom config
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/

# Copy built files
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
