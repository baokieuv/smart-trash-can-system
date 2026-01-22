import React, { useState } from 'react';
import { Activity, Clock, ChevronDown } from 'lucide-react';
import { ActivityLog } from '@/types';
import { getLogIcon } from '@/lib/utils';

interface ActivityFeedProps {
  logs: ActivityLog[];
}

export default function ActivityFeed({ logs }: ActivityFeedProps) {
  const [showAll, setShowAll] = useState(false);
  const displayedLogs = showAll ? logs : logs.slice(0, 5);
  const hasMore = logs.length > 5;

  return (
    <div className="bg-white rounded-2xl shadow-lg p-6 sticky top-24">
      <h2 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
        <Activity size={20} />
        Activity Feed
      </h2>
      <div className="space-y-3">
        {displayedLogs.map(log => (
          <div key={log.id} className="flex gap-3 p-3 bg-slate-50 rounded-lg hover:bg-slate-100 transition-colors">
            <div className="text-xl">{getLogIcon(log.type)}</div>
            <div className="flex-1 min-w-0">
              <div className="font-semibold text-slate-800 text-sm truncate">{log.deviceName}</div>
              <div className="text-xs text-slate-600 mt-0.5">{log.message}</div>
              <div className="flex items-center gap-1 text-xs text-slate-400 mt-1">
                <Clock size={10} />
                {new Date(log.timestamp).toLocaleString()}
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {hasMore && (
        <button
          onClick={() => setShowAll(!showAll)}
          className="w-full mt-4 py-2 text-sm text-blue-600 hover:text-blue-700 font-medium flex items-center justify-center gap-1 hover:bg-blue-50 rounded-lg transition-colors"
        >
          {showAll ? 'View less' : 'View more'}
          <ChevronDown 
            size={16} 
            className={`transition-transform ${showAll ? 'rotate-180' : ''}`}
          />
        </button>
      )}
    </div>
  );
}