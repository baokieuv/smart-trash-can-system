export default function Loading() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 flex items-center justify-center">
      <div className="text-center">
        {/* Animated Spinner với Multiple Layers */}
        <div className="relative inline-block mb-6">
          {/* Outer Ring */}
          <div className="absolute inset-0 animate-spin rounded-full h-20 w-20 border-4 border-blue-200"></div>
          {/* Middle Ring */}
          <div className="absolute inset-2 animate-spin rounded-full h-16 w-16 border-4 border-blue-400 border-t-transparent" style={{ animationDirection: 'reverse', animationDuration: '1s' }}></div>
          {/* Inner Ring */}
          <div className="relative animate-spin rounded-full h-20 w-20 border-4 border-blue-600 border-t-transparent"></div>
          {/* Center Glow */}
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-8 h-8 bg-blue-500 rounded-full blur-md opacity-50 animate-pulse"></div>
          </div>
        </div>
        
        {/* Loading Text */}
        <div className="space-y-2">
          <p className="text-slate-800 font-bold text-lg">Loading Smart Bins</p>
          <p className="text-slate-500 text-sm">Preparing your dashboard...</p>
        </div>

        {/* Animated Dots */}
        <div className="flex justify-center gap-1.5 mt-6">
          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
        </div>
      </div>
    </div>
  );
}