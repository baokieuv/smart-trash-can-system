"use client"

import React, { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { CheckCircle, XCircle, Loader2, Mail, Trash2 } from 'lucide-react';
import Link from 'next/link';

export default function VerifyEmailPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');
  const [countdown, setCountdown] = useState(5);
  const [email, setEmail] = useState('');
  const [isResending, setIsResending] = useState(false);

  useEffect(() => {
    const verifyEmail = async () => {
      const token = searchParams.get('token');
      
      if (!token) {
        setStatus('error');
        setMessage('Verification token is missing. Please check your email link.');
        return;
      }

      try {
        const response = await fetch(`/api/auth/verify-email?token=${token}`);
        const data = await response.json();

        if (response.ok) {
          setStatus('success');
          setMessage('Your email has been verified successfully!');
          
          // Start countdown
          const timer = setInterval(() => {
            setCountdown((prev) => {
              if (prev <= 1) {
                clearInterval(timer);
                router.push('/login');
                return 0;
              }
              return prev - 1;
            });
          }, 1000);

          return () => clearInterval(timer);
        } else {
          setStatus('error');
          setMessage(data.error || 'Email verification failed. The link may be expired or invalid.');
        }
      } catch (error) {
        setStatus('error');
        setMessage('An error occurred while verifying your email. Please try again.');
        console.error('Verification error:', error);
      }
    };

    verifyEmail();
  }, [searchParams, router]);

  const handleResendVerification = async () => {
    if (!email) {
      setMessage('Please enter your email address to resend verification');
      return;
    }

    setIsResending(true);
    
    try {
      const response = await fetch('/api/auth/resend-verification', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage('Verification email sent successfully! Please check your inbox.');
      } else {
        setMessage(data.error || 'Failed to resend verification email');
      }
    } catch (error) {
      setMessage('An error occurred while resending verification email');
      console.error('Resend error:', error);
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="bg-blue-600 p-4 rounded-2xl mb-4">
            <Trash2 className="text-white" size={48} />
          </div>
          <h1 className="text-3xl font-bold text-slate-800">Smart Bin IoT</h1>
          <p className="text-slate-600 mt-2">Email Verification</p>
        </div>

        {/* Verification Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="flex flex-col items-center">
            {/* Loading State */}
            {status === 'loading' && (
              <>
                <div className="bg-blue-100 p-4 rounded-full mb-6">
                  <Loader2 className="text-blue-600 animate-spin" size={48} />
                </div>
                <h2 className="text-2xl font-bold text-slate-800 mb-2">
                  Verifying Your Email
                </h2>
                <p className="text-slate-600 text-center">
                  Please wait while we verify your email address...
                </p>
              </>
            )}

            {/* Success State */}
            {status === 'success' && (
              <>
                <div className="bg-green-100 p-4 rounded-full mb-6">
                  <CheckCircle className="text-green-600" size={48} />
                </div>
                <h2 className="text-2xl font-bold text-slate-800 mb-2">
                  Email Verified!
                </h2>
                <p className="text-slate-600 text-center mb-4">
                  {message}
                </p>
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 w-full text-center">
                  <p className="text-blue-800 font-semibold">
                    Redirecting to login in {countdown} second{countdown !== 1 ? 's' : ''}...
                  </p>
                </div>
                <Link 
                  href="/login"
                  className="mt-4 text-blue-600 hover:text-blue-700 font-semibold text-sm"
                >
                  Click here if you're not redirected
                </Link>
              </>
            )}

            {/* Error State */}
            {status === 'error' && (
              <>
                <div className="bg-red-100 p-4 rounded-full mb-6">
                  <XCircle className="text-red-600" size={48} />
                </div>
                <h2 className="text-2xl font-bold text-slate-800 mb-2">
                  Verification Failed
                </h2>
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 w-full">
                  <p className="text-sm text-red-800 text-center">
                    {message}
                  </p>
                </div>

                {/* Resend Verification Section */}
                <div className="w-full space-y-4">
                  <div className="border-t border-slate-200 pt-6">
                    <p className="text-sm text-slate-600 text-center mb-4">
                      Need a new verification link?
                    </p>
                    
                    <div className="relative mb-4">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                      <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full pl-11 pr-4 py-3 border-2 border-slate-300 rounded-xl text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none transition-colors font-medium"
                        placeholder="your@email.com"
                        disabled={isResending}
                      />
                    </div>

                    <button
                      onClick={handleResendVerification}
                      disabled={isResending}
                      className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {isResending ? 'Sending...' : 'Resend Verification Email'}
                    </button>
                  </div>
                </div>

                <Link 
                  href="/login"
                  className="mt-6 text-blue-600 hover:text-blue-700 font-semibold text-sm"
                >
                  Back to Login
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}