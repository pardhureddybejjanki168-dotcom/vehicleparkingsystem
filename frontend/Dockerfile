# Multi-stage Docker build for React + Vite Frontend
FROM node:20-alpine AS build
WORKDIR /app

# Cache node dependencies
COPY package*.json ./
RUN npm install

# Build static assets
COPY . .
RUN npm run build

# Serve via high-performance Nginx Alpine
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
