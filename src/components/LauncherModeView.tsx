import React, { useState } from 'react';
import {
  Home,
  AppWindow,
  ShieldCheck,
  AlertCircle,
  Settings,
  CheckCircle2,
  ExternalLink,
} from 'lucide-react';
import {
  evaluateLauncherModeStatus,
  type DeviceFirmwareProfile,
  type OperatingMode,
} from '../utils/launcherLogic';

interface LauncherModeViewProps {
  selectedMode: OperatingMode;
  firmwareProfile: DeviceFirmwareProfile;
  userGrantedSystemDefaultHome: boolean;
  accentHex: string;
  glassIntensity: number;
  onSelectMode: (mode: OperatingMode) => void;
  onChangeFirmwareProfile: (profile: DeviceFirmwareProfile) => void;
  onSetDefaultHomeGranted: (granted: boolean) => void;
  onReturnToDashboard: () => void;
}

export const LauncherModeView: React.FC<LauncherModeViewProps> = ({
  selectedMode,
  firmwareProfile,
  userGrantedSystemDefaultHome,
  accentHex,
  glassIntensity,
  onSelectMode,
  onChangeFirmwareProfile,
  onSetDefaultHomeGranted,
  onReturnToDashboard,
}) => {
  const [showSystemHomeChooserModal, setShowSystemHomeChooserModal] = useState(false);
  const [systemSettingsFeedback, setSystemSettingsFeedback] = useState<string | null>(null);

  const evaluation = evaluateLauncherModeStatus(
    selectedMode,
    firmwareProfile,
    userGrantedSystemDefaultHome
  );

  const handleSetAsDefaultClick = () => {
    setSystemSettingsFeedback(null);
    if (evaluation.isFirmwareRestricted) {
      setSystemSettingsFeedback(
        'Compatibility Notice: This Google TV firmware disables android.settings.HOME_SETTINGS for third-party packages. TV LAUNCHER remains 100% functional in NORMAL APPLICATION mode without forcing unsupported modifications.'
      );
      return;
    }
    setShowSystemHomeChooserModal(true);
  };

  const handleOpenSystemSettings = () => {
    setSystemSettingsFeedback(
      'Opened Android TV System Settings (android.settings.SETTINGS) -> Apps -> Default Apps -> Home App.'
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <p className="text-xs font-mono text-[#AAB4C8] tracking-wide">
            DUAL-MODE ARCHITECTURE · COM.SHANAWAZ.TVLAUNCHER
          </p>
          <h1 className="text-3xl font-display font-bold text-white mt-1">
            LAUNCHER MODE
          </h1>
          <p className="text-sm text-[#AAB4C8] mt-1 max-w-2xl">
            Configure TV LAUNCHER as your default Android TV Home interface (Mode 1) or run it on-demand as a normal installed TV application (Mode 2). Both modes share the complete Liquid Glass dashboard.
          </p>
        </div>

        <button
          type="button"
          onClick={onReturnToDashboard}
          className="px-4 py-2.5 rounded-xl text-sm font-medium text-white border border-white/20 hover:bg-white/10 transition-colors whitespace-nowrap shrink-0"
        >
          Open Liquid Glass Dashboard
        </button>
      </div>

      {/* Current Mode Status & Compatibility Telemetry */}
      <div
        className="rounded-[24px] p-6 border border-white/15"
        style={{
          background: `linear-gradient(145deg, rgba(255,255,255,${glassIntensity * 0.14}) 0%, rgba(17,24,39,0.82) 100%)`,
          backdropFilter: `blur(${Math.round(glassIntensity * 18)}px)`,
        }}
      >
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-white/10">
          <div className="space-y-1">
            <div className="text-xs font-mono text-[#AAB4C8]">
              CURRENT OPERATING STATUS
            </div>
            <div className="text-lg font-display font-semibold text-white flex items-center gap-2">
              {evaluation.isSystemDefaultHome ? (
                <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0" />
              ) : (
                <AlertCircle className="w-5 h-5 text-sky-400 shrink-0" />
              )}
              <span>{evaluation.statusBadge}</span>
            </div>
            <p className="text-sm text-[#AAB4C8]">{evaluation.compatibilityMessage}</p>
          </div>

          <div className="flex flex-wrap items-center gap-3 shrink-0">
            <button
              type="button"
              onClick={handleSetAsDefaultClick}
              style={{ backgroundColor: accentHex }}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-950 hover:opacity-95 transition-opacity whitespace-nowrap shrink-0"
            >
              SET AS DEFAULT LAUNCHER
            </button>

            <button
              type="button"
              onClick={handleOpenSystemSettings}
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium text-white bg-white/10 border border-white/15 hover:bg-white/15 transition-colors whitespace-nowrap shrink-0"
            >
              <Settings className="w-4 h-4" />
              <span>System Settings Shortcut</span>
            </button>
          </div>
        </div>

        {/* Platform Detection & Hardware Profile Simulator */}
        <div className="pt-5 grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
          <div>
            <span className="text-[#AAB4C8] block">Active System Home Package</span>
            <span className="font-mono text-white font-medium mt-1 block">
              {evaluation.currentSystemHomePackage}
            </span>
          </div>
          <div>
            <span className="text-[#AAB4C8] block">Manifest Intent Categories</span>
            <span className="font-mono text-white font-medium mt-1 block">
              HOME · DEFAULT · LEANBACK_LAUNCHER
            </span>
          </div>
          <div>
            <label htmlFor="firmware-profile-select" className="text-[#AAB4C8] block">
              Test Target TV Firmware Compatibility
            </label>
            <select
              id="firmware-profile-select"
              value={firmwareProfile}
              onChange={(e) =>
                onChangeFirmwareProfile(e.target.value as DeviceFirmwareProfile)
              }
              className="mt-1 w-full bg-[#080B14]/90 text-white border border-white/20 rounded-lg px-2.5 py-1.5 font-mono text-xs focus:outline-none"
            >
              <option value="STANDARD_ANDROID_TV">
                Android TV OS (Home Selection Supported)
              </option>
              <option value="GOOGLE_TV_UNLOCKED">
                Google TV (Custom Home Enabled)
              </option>
              <option value="GOOGLE_TV_RESTRICTED_HOME">
                Restricted Google TV (Locked Home Firmware)
              </option>
            </select>
          </div>
        </div>

        {systemSettingsFeedback && (
          <div className="mt-4 p-3.5 rounded-xl bg-white/10 border border-white/20 text-xs text-white flex items-center justify-between gap-4">
            <span>{systemSettingsFeedback}</span>
            <button
              type="button"
              onClick={() => setSystemSettingsFeedback(null)}
              className="text-[#AAB4C8] hover:text-white font-medium whitespace-nowrap"
            >
              Dismiss
            </button>
          </div>
        )}
      </div>

      {/* Mode Selection Cards: Option 1 (DEFAULT LAUNCHER) & Option 2 (NORMAL APPLICATION) */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Option 1: DEFAULT LAUNCHER */}
        <button
          type="button"
          onClick={() => onSelectMode('DEFAULT_LAUNCHER')}
          className="text-left rounded-[24px] p-6 transition-all focus:outline-none"
          style={{
            background:
              selectedMode === 'DEFAULT_LAUNCHER'
                ? `linear-gradient(145deg, rgba(255,255,255,0.18) 0%, ${accentHex}24 60%, rgba(17,24,39,0.9) 100%)`
                : 'rgba(17, 24, 39, 0.72)',
            border:
              selectedMode === 'DEFAULT_LAUNCHER'
                ? `2px solid ${accentHex}`
                : '1px solid rgba(255, 255, 255, 0.14)',
          }}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-[#AAB4C8]">
              OPTION 1 · MODE 1
            </span>
            <span className="text-xs font-medium text-sky-300">
              {selectedMode === 'DEFAULT_LAUNCHER' ? 'Selected' : 'Select Mode'}
            </span>
          </div>

          <div className="mt-3 flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-sky-500/20 border border-sky-400/30 flex items-center justify-center">
              <Home className="w-5 h-5 text-sky-300" />
            </div>
            <div>
              <h2 className="text-xl font-display font-bold text-white">
                DEFAULT LAUNCHER
              </h2>
              <p className="text-xs text-[#AAB4C8]">
                Replaces Home button destination via Android System Settings
              </p>
            </div>
          </div>

          <p className="mt-4 text-sm text-white/90 leading-relaxed">
            Use TV LAUNCHER as your television&apos;s Home interface.
          </p>

          <div className="mt-4 pt-4 border-t border-white/10 text-xs text-[#AAB4C8] space-y-1">
            <div>• Declares MAIN + HOME + DEFAULT intent categories</div>
            <div>• Pressing the TV Remote Home button opens TV LAUNCHER</div>
            <div>• Zero root, zero ADB hacks, zero forced overrides</div>
          </div>
        </button>

        {/* Option 2: NORMAL APPLICATION */}
        <button
          type="button"
          onClick={() => onSelectMode('NORMAL_APPLICATION')}
          className="text-left rounded-[24px] p-6 transition-all focus:outline-none"
          style={{
            background:
              selectedMode === 'NORMAL_APPLICATION'
                ? `linear-gradient(145deg, rgba(255,255,255,0.18) 0%, ${accentHex}24 60%, rgba(17,24,39,0.9) 100%)`
                : 'rgba(17, 24, 39, 0.72)',
            border:
              selectedMode === 'NORMAL_APPLICATION'
                ? `2px solid ${accentHex}`
                : '1px solid rgba(255, 255, 255, 0.14)',
          }}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-[#AAB4C8]">
              OPTION 2 · MODE 2
            </span>
            <span className="text-xs font-medium text-sky-300">
              {selectedMode === 'NORMAL_APPLICATION' ? 'Selected' : 'Select Mode'}
            </span>
          </div>

          <div className="mt-3 flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-indigo-500/20 border border-indigo-400/30 flex items-center justify-center">
              <AppWindow className="w-5 h-5 text-indigo-300" />
            </div>
            <div>
              <h2 className="text-xl font-display font-bold text-white">
                NORMAL APPLICATION
              </h2>
              <p className="text-xs text-[#AAB4C8]">
                Standard Android TV / Google TV Leanback App
              </p>
            </div>
          </div>

          <p className="mt-4 text-sm text-white/90 leading-relaxed">
            Open TV LAUNCHER manually without changing your current Home application.
          </p>

          <div className="mt-4 pt-4 border-t border-white/10 text-xs text-[#AAB4C8] space-y-1">
            <div>• Listed in the installed TV application drawer (LEANBACK_LAUNCHER)</div>
            <div>• Works on every Google TV & restricted manufacturer television</div>
            <div>• Full access to Liquid Glass dock, app library, search & wallpapers</div>
          </div>
        </button>
      </div>

      {/* Instructions for Unsupported / Restricted Devices */}
      <div className="rounded-[24px] p-6 bg-[#111827]/80 border border-white/15 space-y-3">
        <h3 className="text-lg font-display font-semibold text-white">
          Setup Instructions & Compatibility Handling
        </h3>
        <div className="space-y-2 text-sm text-[#AAB4C8]">
          {evaluation.manualInstructions.map((line) => (
            <p key={line}>{line}</p>
          ))}
        </div>
      </div>

      {/* Android System Home-App Chooser Simulation Dialog */}
      {showSystemHomeChooserModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="system-home-chooser-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4"
        >
          <div className="max-w-lg w-full rounded-[26px] bg-[#111827] border border-white/25 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-sky-400">
                ANDROID TV SYSTEM INTENT · ACTION_HOME_SETTINGS
              </span>
              <ExternalLink className="w-4 h-4 text-[#AAB4C8]" />
            </div>

            <div>
              <h3
                id="system-home-chooser-title"
                className="text-xl font-display font-bold text-white"
              >
                Select Default Home Application
              </h3>
              <p className="text-sm text-[#AAB4C8] mt-1">
                Choose which application opens when you press the Home button on your Android TV remote:
              </p>
            </div>

            <div className="space-y-2.5">
              <button
                type="button"
                onClick={() => {
                  onSelectMode('DEFAULT_LAUNCHER');
                  onSetDefaultHomeGranted(true);
                  setShowSystemHomeChooserModal(false);
                }}
                className="w-full p-4 rounded-2xl bg-sky-500/15 border border-sky-400/50 text-left flex items-center justify-between hover:bg-sky-500/25 transition-colors"
              >
                <div>
                  <div className="font-display font-semibold text-white">
                    TV LAUNCHER (Always)
                  </div>
                  <div className="text-xs text-[#AAB4C8] font-mono">
                    com.shanawaz.tvlauncher · Liquid Glass Home
                  </div>
                </div>
                <CheckCircle2 className="w-5 h-5 text-sky-400" />
              </button>

              <button
                type="button"
                onClick={() => {
                  onSetDefaultHomeGranted(false);
                  setShowSystemHomeChooserModal(false);
                }}
                className="w-full p-4 rounded-2xl bg-white/5 border border-white/15 text-left flex items-center justify-between hover:bg-white/10 transition-colors"
              >
                <div>
                  <div className="font-display font-semibold text-white">
                    System Stock Android TV Home
                  </div>
                  <div className="text-xs text-[#AAB4C8] font-mono">
                    com.google.android.tvlauncher (Keep TV LAUNCHER as Normal App)
                  </div>
                </div>
              </button>
            </div>

            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => setShowSystemHomeChooserModal(false)}
                className="px-4 py-2 rounded-xl text-xs font-medium text-[#AAB4C8] hover:text-white"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
