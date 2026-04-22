# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Vite: set at build (empty = use Vite default / authRefresh.js fallbacks, often localhost:4000)
ARG VITE_GRAPHQL_URI
ENV VITE_GRAPHQL_URI=${VITE_GRAPHQL_URI}
# Vite builds the static files into the /dist folder
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:alpine
# Copy the built static files from Stage 1 into Nginx's default public folder
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
