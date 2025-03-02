FROM node:22-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build
RUN npm prune --production

FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/build build/
COPY package.json .
EXPOSE 3000
ENV NODE_ENV=production
CMD ["node", "build"]