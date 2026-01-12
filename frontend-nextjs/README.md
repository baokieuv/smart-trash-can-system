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
- Upload ảnh để phân loại rác
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

### 📸 Image Classification
- ✅ Upload ảnh để classify
- ✅ Drag & drop upload
- ✅ Image preview
- ✅ Kết quả classification (label, confidence, category)
- ✅ Lịch sử phân loại

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
| **Axios** | 1.x | HTTP client |
| **React Hook Form** | 7.x | Form handling |
| **Zod** | 3.x | Schema validation |
| **date-fns** | 2.x | Date formatting |
| **React Query** | (optional) | Data fetching & caching |
| **Zustand** | (optional) | State management |

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
git clone https://github.com/your-username/smart-bin-system.git
cd smart-bin-system/frontend-nextjs
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

## ⚙️ Cấu hình

### Environment Variables

Tạo file `.env.local` trong thư mục root:

```env
# API Backend URL
NEXT_PUBLIC_API_URL=http://localhost:8080

# Keycloak
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8081
NEXT_PUBLIC_KEYCLOAK_REALM=smart-bin-realm
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=smart-bin-client

# App Info
NEXT_PUBLIC_APP_NAME=Smart Bin System
NEXT_PUBLIC_APP_VERSION=1.0.0

# Optional: Analytics
NEXT_PUBLIC_GA_ID=G-XXXXXXXXXX
```

### TypeScript Configuration

File `tsconfig.json`:

```json
{
  "compilerOptions": {
    "target": "ES2017",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

### Tailwind Configuration

File `tailwind.config.ts`:

```typescript
import type { Config } from "tailwindcss"

const config = {
  darkMode: ["class"],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
  ],
  prefix: "",
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        // ... other colors
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
} satisfies Config

export default config
```

---

## 📂 Cấu trúc dự án

```
frontend-nextjs/
│
├── public/                      # Static files
│   ├── images/
│   ├── icons/
│   └── favicon.ico
│
├── src/
│   ├── app/                     # Next.js App Router
│   │   ├── (auth)/             # Auth layout group
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   ├── register/
│   │   │   │   └── page.tsx
│   │   │   └── verify-email/
│   │   │       └── page.tsx
│   │   │
│   │   ├── (dashboard)/        # Dashboard layout group
│   │   │   ├── layout.tsx      # Sidebar + navbar
│   │   │   ├── page.tsx        # Home/Dashboard
│   │   │   ├── devices/
│   │   │   │   ├── page.tsx
│   │   │   │   ├── [id]/
│   │   │   │   │   └── page.tsx
│   │   │   │   └── new/
│   │   │   │       └── page.tsx
│   │   │   ├── classify/
│   │   │   │   └── page.tsx
│   │   │   ├── history/
│   │   │   │   └── page.tsx
│   │   │   ├── notifications/
│   │   │   │   └── page.tsx
│   │   │   └── settings/
│   │   │       └── page.tsx
│   │   │
│   │   ├── layout.tsx          # Root layout
│   │   ├── globals.css         # Global styles
│   │   └── error.tsx           # Error boundary
│   │
│   ├── components/              # React components
│   │   ├── ui/                 # shadcn/ui components
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── input.tsx
│   │   │   └── ...
│   │   │
│   │   ├── layout/             # Layout components
│   │   │   ├── Navbar.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Footer.tsx
│   │   │
│   │   ├── auth/               # Auth components
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   │
│   │   ├── devices/            # Device components
│   │   │   ├── DeviceCard.tsx
│   │   │   ├── DeviceList.tsx
│   │   │   ├── DeviceForm.tsx
│   │   │   └── DeviceDetail.tsx
│   │   │
│   │   ├── classify/           # Classification components
│   │   │   ├── ImageUpload.tsx
│   │   │   ├── ClassificationResult.tsx
│   │   │   └── HistoryTable.tsx
│   │   │
│   │   └── shared/             # Shared components
│   │       ├── Loading.tsx
│   │       ├── ErrorMessage.tsx
│   │       └── Stats.tsx
│   │
│   ├── lib/                    # Utilities & helpers
│   │   ├── api/                # API clients
│   │   │   ├── auth.ts
│   │   │   ├── devices.ts
│   │   │   ├── classification.ts
│   │   │   └── notifications.ts
│   │   │
│   │   ├── utils.ts            # Utility functions
│   │   ├── constants.ts        # App constants
│   │   └── axios.ts            # Axios instance
│   │
│   ├── types/                  # TypeScript types
│   │   ├── auth.ts
│   │   ├── device.ts
│   │   ├── classification.ts
│   │   └── notification.ts
│   │
│   ├── hooks/                  # Custom React hooks
│   │   ├── useAuth.ts
│   │   ├── useDevices.ts
│   │   ├── useClassify.ts
│   │   └── useNotifications.ts
│   │
│   └── store/                  # State management (optional)
│       ├── authStore.ts
│       └── deviceStore.ts
│
├── .env.local                  # Environment variables
├── .env.example               # Env template
├── .eslintrc.json             # ESLint config
├── .gitignore
├── next.config.js             # Next.js config
├── package.json
├── tsconfig.json              # TypeScript config
├── tailwind.config.ts         # Tailwind config
├── postcss.config.js
├── components.json            # shadcn/ui config
└── README.md                  # This file
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
/devices/new               → Add new device
/devices/[id]              → Device detail
/classify                  → Image classification
/history                   → Classification history
/notifications             → Notifications
/settings                  → User settings
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

## 🔐 Authentication

### Auth Hook

File `src/hooks/useAuth.ts`:

```typescript
'use client'

import { useState, useEffect } from 'react'
import { authApi } from '@/lib/api/auth'
import type { User } from '@/types/auth'

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    checkAuth()
  }, [])

  const checkAuth = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        setIsAuthenticated(false)
        setIsLoading(false)
        return
      }

      const userData = await authApi.getCurrentUser()
      setUser(userData)
      setIsAuthenticated(true)
    } catch (error) {
      setIsAuthenticated(false)
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    } finally {
      setIsLoading(false)
    }
  }

  const login = async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    
    localStorage.setItem('accessToken', response.accessToken)
    localStorage.setItem('refreshToken', response.refreshToken)
    
    setUser(response.user)
    setIsAuthenticated(true)
    
    return response
  }

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      await authApi.logout(refreshToken || '')
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      setUser(null)
      setIsAuthenticated(false)
    }
  }

  const register = async (data: RegisterData) => {
    return await authApi.register(data)
  }

  return {
    user,
    isLoading,
    isAuthenticated,
    login,
    logout,
    register,
    checkAuth,
  }
}
```

---

## 📡 API Integration

### Axios Instance

File `src/lib/axios.ts`:

```typescript
import axios, { AxiosError } from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - thêm token vào header
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        
        const response = await axios.post(
          `${process.env.NEXT_PUBLIC_API_URL}/api/v1/auth/refresh`,
          { refreshToken }
        )

        const { accessToken } = response.data

        localStorage.setItem('accessToken', accessToken)

        originalRequest.headers.Authorization = `Bearer ${accessToken}`

        return apiClient(originalRequest)
      } catch (refreshError) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
```

### Auth API

File `src/lib/api/auth.ts`:

```typescript
import apiClient from '../axios'
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '@/types/auth'

export const authApi = {
  register: async (data: RegisterRequest) => {
    const response = await apiClient.post('/api/v1/auth/register', data)
    return response.data
  },

  login: async (email: string, password: string): Promise<AuthResponse> => {
    const response = await apiClient.post('/api/v1/auth/login', {
      email,
      password,
    })
    return response.data
  },

  logout: async (refreshToken: string) => {
    const response = await apiClient.post('/api/v1/auth/logout', {
      refreshToken,
    })
    return response.data
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get('/api/v1/auth/me')
    return response.data
  },

  changePassword: async (
    currentPassword: string,
    newPassword: string,
    confirmPassword: string
  ) => {
    const response = await apiClient.post('/api/v1/auth/change-password', {
      currentPassword,
      newPassword,
      confirmPassword,
    })
    return response.data
  },

  verifyEmail: async (token: string) => {
    const response = await apiClient.get(
      `/api/v1/auth/verify-email?token=${token}`
    )
    return response.data
  },

  resendVerification: async (email: string) => {
    const response = await apiClient.post('/api/v1/auth/resend-verification', {
      email,
    })
    return response.data
  },
}
```

### Device API

File `src/lib/api/devices.ts`:

```typescript
import apiClient from '../axios'
import type { Device, CreateDeviceRequest } from '@/types/device'

export const deviceApi = {
  getAll: async (): Promise<Device[]> => {
    const response = await apiClient.get('/api/v1/devices')
    return response.data
  },

  getById: async (id: string): Promise<Device> => {
    const response = await apiClient.get(`/api/v1/devices/${id}`)
    return response.data
  },

  create: async (data: CreateDeviceRequest): Promise<Device> => {
    const response = await apiClient.post('/api/v1/devices', data)
    return response.data
  },

  update: async (id: string, data: Partial<Device>): Promise<Device> => {
    const response = await apiClient.put(`/api/v1/devices/${id}`, data)
    return response.data
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/devices/${id}`)
  },

  getData: async (id: string) => {
    const response = await apiClient.get(`/api/v1/devices/${id}/data`)
    return response.data
  },
}
```

### Classification API

File `src/lib/api/classification.ts`:

```typescript
import apiClient from '../axios'

export const classificationApi = {
  classify: async (imageFile: File) => {
    const formData = new FormData()
    formData.append('image', imageFile)

    const response = await apiClient.post('/api/v1/classify-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  },
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

### Lint & Format

```bash
# ESLint
npm run lint

# Type check
npx tsc --noEmit

# Format với Prettier (nếu có)
npm run format
```

---

## 🌐 Build & Deploy

### Build for Production

```bash
npm run build
```

Output trong thư mục `.next/`

### Deploy lên Vercel

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel

# Production deployment
vercel --prod
```

Hoặc connect GitHub repo với Vercel dashboard.

### Deploy lên Netlify

```bash
# Install Netlify CLI
npm i -g netlify-cli

# Build
npm run build

# Deploy
netlify deploy --prod --dir=.next
```

### Deploy lên VPS (với PM2)

```bash
# Build
npm run build

# Start với PM2
pm2 start npm --name "smart-bin-frontend" -- start

# Save PM2 config
pm2 save
pm2 startup
```

### Docker

```dockerfile
FROM node:18-alpine AS base

# Dependencies
FROM base AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

# Builder
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Runner
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000

CMD ["node", "server.js"]
```

Build & Run:

```bash
docker build -t smart-bin-frontend .
docker run -p 3000:3000 smart-bin-frontend
```

---

## 🧪 Testing

### Unit Tests (với Jest)

```bash
npm install --save-dev jest @testing-library/react @testing-library/jest-dom

# Run tests
npm test
```

### E2E Tests (với Playwright)

```bash
npm install --save-dev @playwright/test

# Run E2E tests
npx playwright test
```

---

## ⚡ Performance Optimization

### Image Optimization

```tsx
import Image from 'next/image'

<Image
  src="/logo.png"
  width={200}
  height={100}
  alt="Logo"
  priority
/>
```

### Code Splitting

```tsx
import dynamic from 'next/dynamic'

const DynamicComponent = dynamic(() => import('@/components/Heavy'), {
  loading: () => <p>Loading...</p>,
  ssr: false,
})
```

### Caching với React Query (Optional)

```bash
npm install @tanstack/react-query
```

```tsx
import { useQuery } from '@tanstack/react-query'

function Devices() {
  const { data, isLoading } = useQuery({
    queryKey: ['devices'],
    queryFn: () => deviceApi.getAll(),
    staleTime: 30000, // 30s
  })

  // ...
}
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