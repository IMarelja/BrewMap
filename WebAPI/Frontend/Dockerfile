FROM node:20-alpine
WORKDIR /app
COPY package.json ./
RUN npm install -g pnpm
RUN pnpm install
COPY package.json pnpm-lock.yaml ./
EXPOSE 3000
CMD ["npm", "run", "dev"]