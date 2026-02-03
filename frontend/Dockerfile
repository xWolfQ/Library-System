# ===== STAGE 1: build React app =====
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build


# ===== STAGE 2: nginx =====
FROM nginx:alpine

# usuwamy domyślną konfigurację
RUN rm /etc/nginx/conf.d/default.conf

# własna konfiguracja nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# kopiujemy build Reacta
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
