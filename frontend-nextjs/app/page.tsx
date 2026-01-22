"use client"

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { Trash2 } from 'lucide-react';

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading) {
      if (isAuthenticated) {
        router.push('/dashboard');
      } else {
        router.push('/login');
      }
    }
  }, [isAuthenticated, loading, router]);

  return (
    <div className="flex h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-slate-100">
      <div className="text-center">
        <div className="bg-blue-600 p-6 rounded-2xl inline-block mb-4">
          <Trash2 className="text-white" size={64} />
        </div>
        <h1 className="text-4xl font-bold text-slate-800 mb-2">Smart Bin IoT</h1>
        <p className="text-slate-600 mb-6">Loading...</p>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      </div>
    </div>
  );
}