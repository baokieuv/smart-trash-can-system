"use client"

import React, { useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Trash2, Mail, Lock, AlertCircle, CheckCircle } from 'lucide-react';
import Link from 'next/link';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const { login, forgotPassword } = useAuth();
  const [isResetting, setIsResetting] = useState(false);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    const result = await login({ email, password });
    
    setLoading(false);
    
    if (!result.success) {
      setError(result.error || 'Login failed');
    }
  };

  const handleForgotPassword = async () => {
    // e.preventDefault();
    // 1. Validate Email
    if (!email) {
      setError('Please enter your email first to reset password');
      return;
    }

    setError('');
    setSuccessMessage('');
    setIsResetting(true);

    const result = await forgotPassword(email);

    setIsResetting(false);
    if(!result.success){
      setError(result.error || 'Reset password failed');
    }else{
      setSuccessMessage(result.error || 'Password reset successful. Please check your email for the new password.');
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="bg-blue-600 p-4 rounded-2xl mb-4">
            <Trash2 className="text-white" size={48} />
          </div>
          <h1 className="text-3xl font-bold text-slate-800">Smart Bin IoT</h1>
          <p className="text-slate-600 mt-2">Sign in to your account</p>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start gap-3">
                <AlertCircle className="text-red-600 flex-shrink-0" size={20} />
                <div className="flex-1">
                  <p className="text-sm text-red-800">{error}</p>
                  {error.includes('Email not verified') && (
                    <button
                      type="button"
                      className="text-sm text-blue-600 hover:text-blue-700 font-semibold mt-2"
                      onClick={() => {/* TODO: Resend verification */}}
                    >
                      Resend verification email
                    </button>
                  )}
                </div>
              </div>
            )}

            {/* Success Message */}
            {successMessage && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-start gap-3 animate-fade-in">
                <CheckCircle className="text-green-600 flex-shrink-0" size={20} />
                <div className="flex-1">
                  <p className="text-sm text-green-800">{successMessage}</p>
                </div>
              </div>
            )}

            {/* Email Input */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 border-2 border-slate-300 rounded-xl text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none transition-colors font-medium"
                  placeholder="your@email.com"
                  required
                  disabled={loading || isResetting}
                />
              </div>
            </div>

            {/* Password Input */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 border-2 border-slate-300 rounded-xl text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none transition-colors font-medium"
                  placeholder="••••••••"
                  required
                  disabled={loading || isResetting}
                />
              </div>

              {/* Forgot password Input */}
              <div className="flex justify-end mt-2">
                <button
                  type="button"
                  onClick={handleForgotPassword}
                  disabled={loading || isResetting}
                  className="text-sm text-blue-600 hover:text-blue-700 font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isResetting ? 'Sending...' : 'Forgot Password?'}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Register Link */}
          <div className="mt-6 text-center">
            <p className="text-sm text-slate-600">
              Don't have an account?{' '}
              <Link href="/register" className="text-blue-600 hover:text-blue-700 font-semibold">
                Sign up
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}