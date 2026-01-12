"use client"

import ProtectedRoute from '@/components/ProtectedRoute';
import { useAuth } from '@/contexts/AuthContext';
import { LogOut, User, Lock } from 'lucide-react';
import { useState } from 'react';
import Link from 'next/link';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, logout } = useAuth();
  const [showUserMenu, setShowUserMenu] = useState(false);

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        {/* Top Navigation Bar */}
        <div className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
          <div className="max-w-7xl mx-auto px-4 md:px-8 py-3">
            <div className="flex items-center justify-end">
              {/* User Menu */}
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-3 px-4 py-2 rounded-lg hover:bg-slate-50 transition-colors"
                >
                  <div className="bg-blue-600 w-10 h-10 rounded-full flex items-center justify-center">
                    <User className="text-white" size={20} />
                  </div>
                  <div className="text-left hidden md:block">
                    <div className="text-sm font-semibold text-slate-800">
                      {user?.firstName} {user?.lastName}
                    </div>
                    <div className="text-xs text-slate-500">{user?.email}</div>
                  </div>
                </button>

                {/* Dropdown Menu */}
                {showUserMenu && (
                  <>
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setShowUserMenu(false)}
                    />
                    <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-slate-200 py-2 z-20">
                      <div className="px-4 py-3 border-b border-slate-200">
                        <div className="font-semibold text-slate-800">
                          {user?.firstName} {user?.lastName}
                        </div>
                        <div className="text-sm text-slate-500">{user?.email}</div>
                        <div className="mt-2">
                          {user?.emailVerified ? (
                            <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full">
                              ✓ Verified
                            </span>
                          ) : (
                            <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">
                              ⚠ Not verified
                            </span>
                          )}
                        </div>
                      </div>
                      
                      {/* Change Password Link */}
                      <Link
                        href="/dashboard/change-password"
                        onClick={() => setShowUserMenu(false)}
                        className="w-full px-4 py-3 text-left hover:bg-slate-50 transition-colors flex items-center gap-3 text-slate-700"
                      >
                        <Lock size={18} />
                        <span className="font-medium">Change Password</span>
                      </Link>

                      {/* Logout Button */}
                      <button
                        onClick={logout}
                        className="w-full px-4 py-3 text-left hover:bg-slate-50 transition-colors flex items-center gap-3 text-red-600 border-t border-slate-200"
                      >
                        <LogOut size={18} />
                        <span className="font-medium">Logout</span>
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Main Content */}
        {children}
      </div>
    </ProtectedRoute>
  );
}