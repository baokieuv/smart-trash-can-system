# 1. Base image
FROM node:20-alpine AS base

# 2. Deps stage: Chỉ cài dependencies
FROM base AS deps
# Cài thêm libc6-compat nếu dùng thư viện xử lý ảnh (Sharp) trên Alpine
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY package.json package-lock.json* ./
# Dùng npm ci thay vì npm install để đảm bảo version chính xác (từ lock file)
RUN npm ci

# 3. Builder stage: Build source code
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Tắt telemetry của Nextjs cho gọn log
ENV NEXT_TELEMETRY_DISABLED 1

# Nếu app bạn cần biến môi trường Public lúc Build (ví dụ NEXT_PUBLIC_API_URL)
# Thì phải khai báo ARG ở đây. (Xem giải thích ở dưới)
# ARG NEXT_PUBLIC_API_URL
# ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

RUN npm run build

# 4. Runner stage: Chạy ứng dụng
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

# Tạo user system để không chạy bằng quyền root (Bảo mật)
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

# Set quyền owner cho file copy sang
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

# Chuyển sang user thường
USER nextjs

EXPOSE 3000
ENV PORT 3000
# server.js được Next.js tự tạo ra trong folder standalone
CMD ["node", "server.js"]