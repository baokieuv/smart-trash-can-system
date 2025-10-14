# Stage 1: Build app
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Stage 2: Run app
FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy chỉ phần cần thiết
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

RUN npm install --omit=dev

EXPOSE 3001

CMD ["npm", "start"]
