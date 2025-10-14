# Stage 1: Build
FROM node:22-alpine AS builder

WORKDIR /app

# Copy file package.json và lock để tận dụng cache
COPY package*.json ./

RUN npm install

# Copy toàn bộ mã nguồn
COPY . .

# Build project NestJS -> dist/
RUN npm run build


# Stage 2: Runtime
FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy package info và dist từ stage build
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/dist ./dist

# Cài dependency cho production
RUN npm install --omit=dev

EXPOSE 3000

# Chạy app
CMD ["node", "dist/main.js"]
