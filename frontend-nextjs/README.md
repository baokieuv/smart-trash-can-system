# 🌐 Smart Bin Web Frontend (Next.js)

Web application cho hệ thống Smart Bin, được xây dựng bằng Next.js 14 với App Router.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Yêu cầu](#-yêu-cầu)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Routing](#-routing)
- [Authentication](#-authentication)
- [API Integration](#-api-integration)
- [State Management](#-state-management)
- [Styling](#-styling)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Build & Deploy](#-build--deploy)
- [Environment Variables](#-environment-variables)
- [Testing](#-testing)
- [Performance Optimization](#-performance-optimization)

---

## 🎯 Giới thiệu

Smart Bin Web Frontend là ứng dụng web responsive cho phép người dùng:

- Quản lý tài khoản (đăng ký, đăng nhập, xác thực email)
- Quản lý thiết bị Smart Bin
- Xem dashboard thống kê
- Theo dõi lịch sử phân loại
- Nhận thông báo real-time

---

## ✨ Tính năng

### 🔐 Authentication
- ✅ Đăng ký tài khoản mới
- ✅ Email verification
- ✅ Đăng nhập với Keycloak OAuth2
- ✅ Quên mật khẩu (TODO)
- ✅ Đổi mật khẩu
- ✅ Auto refresh token
- ✅ Persistent login với localStorage
- ✅ Protected routes

### 🎛️ Device Management
- ✅ Xem danh sách devices
- ✅ Thêm device mới
- ✅ Cập nhật thông tin device
- ✅ Xóa device
- ✅ Xem chi tiết device (realtime data)
- ✅ Device status indicators (online/offline)

### 📊 Dashboard & Analytics
- ✅ Tổng số devices
- ✅ Devices online/offline
- ✅ Waste statistics (recyclable, organic, non-recyclable)
- ✅ Charts & graphs (TODO)
- ✅ Real-time updates

### 🔔 Notifications
- ✅ Danh sách notifications
- ✅ Notification badges
- ✅ Mark as read (TODO)
- ✅ Filter by type

### 🎨 UI/UX
- ✅ Modern & responsive design
- ✅ Dark mode support
- ✅ Mobile-friendly
- ✅ Loading states
- ✅ Error handling
- ✅ Toast notifications
- ✅ Skeleton loaders

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Next.js** | 14.x | React framework với App Router |
| **React** | 18.x | UI library |
| **TypeScript** | 5.x | Type safety |
| **Tailwind CSS** | 3.x | Utility-first CSS |
| **shadcn/ui** | - | UI components |
| **Radix UI** | - | Headless UI primitives |
| **Lucide React** | - | Icon library |
| **React Hook Form** | 7.x | Form handling |
| **Zod** | 3.x | Schema validation |
| **date-fns** | 2.x | Date formatting |

---

## 📱 Yêu cầu

### Development
- **Node.js**: 18.x hoặc cao hơn
- **npm**: 9.x hoặc **yarn**: 1.22+
- **RAM**: 4GB minimum
- **Browser**: Chrome, Firefox, Safari (latest)

### Production
- **Node.js**: 18.x LTS
- **Server**: Vercel, Netlify, hoặc VPS với Node.js

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/frontend-nextjs
```

### 2. Install dependencies

```bash
npm install
# hoặc
yarn install
# hoặc
pnpm install
```

### 3. Cài đặt shadcn/ui components (nếu chưa có)

```bash
npx shadcn-ui@latest init
```

Chọn các tùy chọn:
- TypeScript: Yes
- Style: Default
- Base color: Slate
- CSS variables: Yes

Thêm components cần thiết:

```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add input
npx shadcn-ui@latest add card
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add dropdown-menu
npx shadcn-ui@latest add table
npx shadcn-ui@latest add toast
npx shadcn-ui@latest add avatar
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add tabs
npx shadcn-ui@latest add form
```
---

## 🗺️ Routing

### App Router Structure

```
/                          → Home/Dashboard
/login                     → Login page
/register                  → Register page
/verify-email?token=xxx    → Email verification
/devices                   → Device list
/devices/[id]              → Device detail
/classify                  → Image classification
/notifications             → Notifications
```

### Route Protection

File `src/app/(dashboard)/layout.tsx`:

```typescript
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const { isAuthenticated, isLoading } = useAuth()

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  if (isLoading) {
    return <div>Loading...</div>
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="flex-1">
        <Navbar />
        <div className="p-6">{children}</div>
      </main>
    </div>
  )
}
```

---

## 🎨 Styling

### Tailwind + shadcn/ui

Components sử dụng Tailwind CSS và shadcn/ui:

```tsx
import { Button } from '@/components/ui/button'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'

export function DeviceCard({ device }: { device: Device }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{device.name}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-2">
          <div className={`h-2 w-2 rounded-full ${
            device.status === 'ONLINE' ? 'bg-green-500' : 'bg-gray-400'
          }`} />
          <span className="text-sm text-muted-foreground">
            {device.status}
          </span>
        </div>
        <Button className="mt-4 w-full">View Details</Button>
      </CardContent>
    </Card>
  )
}
```

### Dark Mode

File `src/app/layout.tsx`:

```typescript
import { ThemeProvider } from '@/components/theme-provider'

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          {children}
        </ThemeProvider>
      </body>
    </html>
  )
}
```

---

## 🚀 Chạy ứng dụng

### Development Mode

```bash
npm run dev
# hoặc
yarn dev
# hoặc
pnpm dev
```

Truy cập: http://localhost:3000

### Production Build

```bash
npm run build
npm run start
```
---

## 🐛 Common Issues

### 1. Hydration mismatch

**Lỗi**: `Text content did not match`

**Giải pháp**:
- Dùng `suppressHydrationWarning` cho dynamic content
- Check client-only rendering với `useEffect`

### 2. Environment variables không load

**Giải pháp**:
- Restart dev server sau khi thay đổi `.env.local`
- Đảm bảo prefix `NEXT_PUBLIC_` cho client-side vars

### 3. CORS error

**Giải pháp**:
- Backend phải enable CORS cho `http://localhost:3000`
- Check `Access-Control-Allow-Origin` header

---

## 📚 Tài liệu tham khảo

- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [shadcn/ui](https://ui.shadcn.com/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

---

<div align="center">
  <p>Made with ❤️ for Smart Bin System</p>
</div>