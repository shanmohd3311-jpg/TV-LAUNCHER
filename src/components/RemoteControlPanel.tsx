import React from 'react';
import {
  ChevronUp,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CornerDownLeft,
  Home,
  Search,
  Settings,
  Sparkles,
  X,
} from 'lucide-react';

interface RemoteControlPanelProps {
  isOpen: boolean;
  accentHex: string;
  onClose: () => void;
  onDpadDirection: (dir: 'UP' | 'DOWN' | 'LEFT' | 'RIGHT') => void;
  onDpadSelect: () => void;
  onDpadBack: () => void;
  onDpadHome: () => void;
  onQuickSearch: () => void;
  onQuickSettings: () => void;
  onTriggerScreensaver: () => void;
}

export const RemoteControlPanel: React.FC<RemoteControlPanelProps> = ({
  isOpen,
  accentHex,
  onClose,
  onDpadDirection,
  onDpadSelect,
  onDpadBack,
  onDpadHome,
  onQuickSearch,
  onQuickSettings,
  onTriggerScreensaver,
}) => {
  if (!isOpen) return null;

  return (
    <aside
      aria-label="Android TV Remote Controller"
      className="fixed bottom-5 right-5 z-40 w-64 rounded-[28px] bg-[#0B101E]/95 border border-white/20 p-5 shadow-2xl backdrop-blur-xl space-y-4"
    >
      <div className="flex items-center justify-between border-b border-white/10 pb-2.5">
        <div>
          <div className="text-xs font-display font-bold text-white">
            TV REMOTE CONTROL
          </div>
          <div className="text-[11px] text-[#AAB4C8]">
            D-Pad / Arrow Keys Active
          </div>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label="Close TV Remote Controller"
          className="p-1 rounded-lg text-[#AAB4C8] hover:text-white hover:bg-white/10"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Circular D-Pad */}
      <div className="relative w-40 h-40 mx-auto rounded-full bg-[#111827] border border-white/15 flex items-center justify-center shadow-inner">
        <button
          type="button"
          onClick={() => onDpadDirection('UP')}
          aria-label="D-Pad Up"
          className="absolute top-2 left-1/2 -translate-x-1/2 p-2 rounded-full text-white hover:bg-white/15 transition-colors"
        >
          <ChevronUp className="w-5 h-5" />
        </button>

        <button
          type="button"
          onClick={() => onDpadDirection('DOWN')}
          aria-label="D-Pad Down"
          className="absolute bottom-2 left-1/2 -translate-x-1/2 p-2 rounded-full text-white hover:bg-white/15 transition-colors"
        >
          <ChevronDown className="w-5 h-5" />
        </button>

        <button
          type="button"
          onClick={() => onDpadDirection('LEFT')}
          aria-label="D-Pad Left"
          className="absolute left-2 top-1/2 -translate-y-1/2 p-2 rounded-full text-white hover:bg-white/15 transition-colors"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        <button
          type="button"
          onClick={() => onDpadDirection('RIGHT')}
          aria-label="D-Pad Right"
          className="absolute right-2 top-1/2 -translate-y-1/2 p-2 rounded-full text-white hover:bg-white/15 transition-colors"
        >
          <ChevronRight className="w-5 h-5" />
        </button>

        <button
          type="button"
          onClick={onDpadSelect}
          aria-label="D-Pad OK Select"
          style={{ backgroundColor: accentHex }}
          className="w-14 h-14 rounded-full text-xs font-display font-bold text-slate-950 shadow-md hover:opacity-95 transition-opacity"
        >
          OK
        </button>
      </div>

      {/* Back / Home / Search / Settings Buttons */}
      <div className="grid grid-cols-2 gap-2 text-xs">
        <button
          type="button"
          onClick={onDpadBack}
          className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/10 hover:bg-white/15 text-white font-medium"
        >
          <CornerDownLeft className="w-3.5 h-3.5" />
          <span>Back</span>
        </button>
        <button
          type="button"
          onClick={onDpadHome}
          className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/10 hover:bg-white/15 text-white font-medium"
        >
          <Home className="w-3.5 h-3.5" />
          <span>Home</span>
        </button>
        <button
          type="button"
          onClick={onQuickSearch}
          className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/10 hover:bg-white/15 text-white font-medium"
        >
          <Search className="w-3.5 h-3.5" />
          <span>Search</span>
        </button>
        <button
          type="button"
          onClick={onQuickSettings}
          className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/10 hover:bg-white/15 text-white font-medium"
        >
          <Settings className="w-3.5 h-3.5" />
          <span>Settings</span>
        </button>
      </div>

      <button
        type="button"
        onClick={onTriggerScreensaver}
        className="w-full flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs text-[#AAB4C8] hover:text-white transition-colors"
      >
        <Sparkles className="w-3.5 h-3.5" />
        <span>Preview Ambient Screensaver</span>
      </button>
    </aside>
  );
};
