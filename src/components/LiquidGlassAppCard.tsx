import React from 'react';
import {
  Play,
  Tv,
  Film,
  Server,
  Music,
  Gamepad2,
  FolderSync,
  Settings,
  AlertTriangle,
  Star,
  EyeOff,
  Eye,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import type { TVAppItem } from '../utils/launcherLogic';

interface LiquidGlassAppCardProps {
  app: TVAppItem;
  isFocused: boolean;
  accentHex: string;
  glassIntensity: number;
  reducedMotion: boolean;
  compact?: boolean;
  showDockReorder?: boolean;
  showHideControl?: boolean;
  onFocusCard: () => void;
  onLaunch: (app: TVAppItem) => void;
  onToggleFavourite: (app: TVAppItem) => void;
  onMoveFavourite?: (app: TVAppItem, direction: -1 | 1) => void;
  onToggleHidden?: (app: TVAppItem) => void;
}

export function renderAppIconGlyph(iconCode: string, className = 'w-7 h-7 text-white') {
  switch (iconCode) {
    case 'tv-launcher':
      return <Tv className={className} />;
    case 'youtube':
    case 'vlc':
      return <Play className={className} />;
    case 'netflix':
    case 'disney':
    case 'prime':
      return <Film className={className} />;
    case 'plex':
    case 'kodi':
      return <Server className={className} />;
    case 'spotify':
      return <Music className={className} />;
    case 'gaming':
    case 'steam':
      return <Gamepad2 className={className} />;
    case 'transfer':
      return <FolderSync className={className} />;
    case 'settings':
      return <Settings className={className} />;
    case 'broken':
      return <AlertTriangle className={className} />;
    default:
      return <Tv className={className} />;
  }
}

export const LiquidGlassAppCard: React.FC<LiquidGlassAppCardProps> = ({
  app,
  isFocused,
  accentHex,
  glassIntensity,
  reducedMotion,
  compact = false,
  showDockReorder = false,
  showHideControl = false,
  onFocusCard,
  onLaunch,
  onToggleFavourite,
  onMoveFavourite,
  onToggleHidden,
}) => {
  const surfaceAlpha = Math.min(0.28, Math.max(0.08, glassIntensity * (isFocused ? 0.22 : 0.11)));

  return (
    <div
      onMouseEnter={onFocusCard}
      className={`group relative rounded-[22px] transition-all ${
        reducedMotion ? 'duration-0' : 'duration-150'
      } ${isFocused ? (reducedMotion ? '' : 'scale-[1.04] z-10') : 'scale-100'}`}
      style={{
        background: isFocused
          ? `linear-gradient(145deg, rgba(255,255,255,${surfaceAlpha + 0.06}) 0%, ${accentHex}26 55%, rgba(17,24,39,0.88) 100%)`
          : `linear-gradient(145deg, rgba(255,255,255,${surfaceAlpha}) 0%, rgba(17,24,39,0.72) 100%)`,
        border: isFocused
          ? `1.5px solid ${accentHex}`
          : '1px solid rgba(255, 255, 255, 0.16)',
        boxShadow: isFocused
          ? `0 14px 34px -8px ${accentHex}45, inset 0 1px 0 0 rgba(255,255,255,0.35)`
          : '0 8px 24px -8px rgba(0,0,0,0.5), inset 0 1px 0 0 rgba(255,255,255,0.14)',
        backdropFilter: `blur(${Math.round(glassIntensity * 20)}px)`,
      }}
    >
      {/* Main Launch Action Button */}
      <button
        type="button"
        onFocus={onFocusCard}
        onClick={() => onLaunch(app)}
        aria-label={`Launch ${app.appName}`}
        className={`w-full text-left focus:outline-none ${
          compact ? 'p-4' : 'p-5'
        } flex flex-col items-start justify-between`}
      >
        <div className="w-full flex items-start justify-between gap-2">
          <div
            className={`${
              compact ? 'w-12 h-12 rounded-2xl' : 'w-14 h-14 rounded-2xl'
            } flex items-center justify-center shrink-0 shadow-md`}
            style={{
              background: `linear-gradient(135deg, ${app.brandColor} 0%, ${app.accentColor} 100%)`,
              border: '1px solid rgba(255,255,255,0.28)',
            }}
          >
            {renderAppIconGlyph(app.iconCode, compact ? 'w-6 h-6 text-white' : 'w-7 h-7 text-white')}
          </div>

          <span className="text-[11px] font-mono text-[#AAB4C8] whitespace-nowrap">
            {app.isLeanbackNative ? 'Leanback TV' : 'Standard APK'}
          </span>
        </div>

        <div className="mt-3.5 w-full">
          <div className="font-display font-semibold text-white text-base truncate">
            {app.appName}
          </div>
          <div className="text-xs text-[#AAB4C8] truncate mt-0.5">
            {compact ? app.category : `${app.category} · v${app.versionName}`}
          </div>
        </div>
      </button>

      {/* Subtle Quick Action Bar for Favourites, Reorder, and Hide */}
      <div className="px-4 pb-3 pt-1 flex items-center justify-between gap-1 border-t border-white/10 text-xs">
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavourite(app);
          }}
          aria-label={
            app.isFavourite
              ? `Remove ${app.appName} from favourites`
              : `Add ${app.appName} to favourites`
          }
          className={`inline-flex items-center gap-1.5 py-1 px-2 rounded-lg transition-colors whitespace-nowrap shrink-0 ${
            app.isFavourite
              ? 'text-sky-300 hover:bg-white/10'
              : 'text-[#AAB4C8] hover:text-white hover:bg-white/10'
          }`}
        >
          <Star
            className="w-3.5 h-3.5"
            fill={app.isFavourite ? 'currentColor' : 'none'}
          />
          <span>{app.isFavourite ? 'Pinned' : 'Pin'}</span>
        </button>

        <div className="flex items-center gap-1">
          {showDockReorder && onMoveFavourite && (
            <>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onMoveFavourite(app, -1);
                }}
                aria-label={`Move ${app.appName} left in dock`}
                className="p-1 rounded-md text-[#AAB4C8] hover:text-white hover:bg-white/10 transition-colors"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onMoveFavourite(app, 1);
                }}
                aria-label={`Move ${app.appName} right in dock`}
                className="p-1 rounded-md text-[#AAB4C8] hover:text-white hover:bg-white/10 transition-colors"
              >
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </>
          )}

          {showHideControl && onToggleHidden && app.packageName !== 'com.shanawaz.tvlauncher' && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleHidden(app);
              }}
              aria-label={app.isHidden ? `Unhide ${app.appName}` : `Hide ${app.appName}`}
              className="inline-flex items-center gap-1 py-1 px-2 rounded-md text-[#AAB4C8] hover:text-white hover:bg-white/10 transition-colors whitespace-nowrap shrink-0"
            >
              {app.isHidden ? <Eye className="w-3.5 h-3.5" /> : <EyeOff className="w-3.5 h-3.5" />}
              <span>{app.isHidden ? 'Unhide' : 'Hide'}</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
