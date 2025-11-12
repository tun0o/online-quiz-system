# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app

# Copy package.json and package-lock.json (or pnpm-lock.yaml)
COPY package*.json ./
# Nếu bạn dùng pnpm, hãy dùng dòng sau thay cho npm install
# RUN npm install -g pnpm && pnpm install
RUN npm install

# Copy the rest of the application source code
COPY . .
RUN npm run build

# Stage 2: Serve the static files with Nginx
FROM nginx:1.25-alpine
# Xóa file cấu hình mặc định của Nginx
RUN rm /etc/nginx/conf.d/default.conf
# Sao chép file cấu hình tùy chỉnh của chúng ta vào
COPY nginx.conf /etc/nginx/conf.d/
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80