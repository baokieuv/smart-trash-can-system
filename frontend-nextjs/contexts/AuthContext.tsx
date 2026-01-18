"use client"

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { User, AuthResponse, LoginCredentials, RegisterData } from '@/types';
import { useRouter } from 'next/navigation';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (credentials: LoginCredentials) => Promise<{ success: boolean; error?: string }>;
  register: (data: RegisterData) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  resendVerification: (email: string) => Promise<{ success: boolean; error?: string }>;
  forgotPassword: (email: string) => Promise<{ success: boolean; error?: string }>;
  isAuthenticated: boolean;
  refreshAccessToken: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const router = useRouter();

  const clearAuth = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('tokenExpiry');
    setUser(null);
  }, []);

  const refreshAccessToken = useCallback(async (): Promise<boolean> => {
    if (refreshing) return false;
    
    setRefreshing(true);
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      clearAuth();
      setRefreshing(false);
      return false;
    }

    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      const data = await response.json();

      if (!response.ok) {
        clearAuth();
        router.push('/login');
        return false;
      }

      // Save new tokens
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      // localStorage.setItem('user', JSON.stringify(data.user));
      
      // Calculate and save token expiry time
      const expiryTime = Date.now() + (data.expiresIn * 1000) - 60000; // Refresh 1 min before expiry
      localStorage.setItem('tokenExpiry', expiryTime.toString());
      
      // setUser(data.user);
      setRefreshing(false);
      return true;
    } catch (error) {
      console.error('Token refresh error:', error);
      clearAuth();
      router.push('/login');
      setRefreshing(false);
      return false;
    }
  }, [refreshing, clearAuth, router]);

  useEffect(() => {
    checkAuth();
    
    // Setup auto-refresh interval
    const interval = setInterval(() => {
      const tokenExpiry = localStorage.getItem('tokenExpiry');
      if (tokenExpiry && Date.now() >= parseInt(tokenExpiry)) {
        refreshAccessToken();
      }
    }, 30000); // Check every 30 seconds

    return () => clearInterval(interval);
  }, [refreshAccessToken]);

  const checkAuth = () => {
    const token = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('user');
    const tokenExpiry = localStorage.getItem('tokenExpiry');
    
    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        setUser(parsedUser);

        // Check if token is expired or about to expire
        if (tokenExpiry && Date.now() >= parseInt(tokenExpiry)) {
          refreshAccessToken();
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
        clearAuth();
      }
    }
    setLoading(false);
  };

  const login = async (credentials: LoginCredentials) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
      });

      const data = await response.json();

      if (!response.ok) {
        return { success: false, error: data.error || 'Login failed' };
      }

      // Save tokens and user data
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      
      // Calculate and save token expiry time (refresh 1 min before actual expiry)
      const expiryTime = Date.now() + (data.expiresIn * 1000) - 60000;
      localStorage.setItem('tokenExpiry', expiryTime.toString());
      
      setUser(data.user);
      router.push('/dashboard');
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: 'Network error. Please try again.' };
    }
  };

  const register = async (data: RegisterData) => {
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (!response.ok) {
        return { success: false, error: result.error || 'Registration failed' };
      }

      return { success: true };
    } catch (error) {
      console.error('Register error:', error);
      return { success: false, error: 'Network error. Please try again.' };
    }
  };

  const logout = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');
      if (token) {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json' 
          },
          body: JSON.stringify({ refreshToken }),
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      clearAuth();
      router.push('/login');
    }
  };

  const resendVerification = async (email: string) => {
    try{
      const response = await fetch("/api/auth/resend-verification", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email }),
      });

      const result = await response.json();

      if (!response.ok) {
        return { success: false, error: result.error || 'Resend verification failed' };
      }

      return { success: true, error: result.message || 'Verification email sent successfully.' };
    }catch(error){
      console.error('Resend verification error:', error);
      return { success: false, error: 'Network error. Please try again.' };
    }
  }

  const forgotPassword = async (email: string) => {
    try{
      const response = await fetch("/api/auth/forgot-password", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email }),
      });

      const result = await response.json();

      if (!response.ok) {
        return { success: false, error: result.error || 'Reset password failed' };
      }

      return { success: true, error: result.message || 'Password reset successful. Please check your email for the new password.' };
    }catch (error){
      console.error('Logout error:', error);
      return { success: false, error: 'Network error. Please try again.' };
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        resendVerification,
        forgotPassword,
        isAuthenticated: !!user,
        refreshAccessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};