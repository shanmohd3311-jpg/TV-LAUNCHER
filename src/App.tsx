import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  Wifi,
  BatteryCharging,
  Search as SearchIcon,
  RefreshCw,
  Upload,
  RotateCcw,
  Gamepad2,
  X,
  CheckCircle2,
  AlertTriangle,
  Sparkles,
  Eye,
} from 'lucide-react';
import {
  INITIAL_TV_APPS,
  BUILT_IN_WALLPAPERS,
  type WallpaperOption,
} from './data/tvAppsData';
import {
  filterAndSortApps,
  getFavouriteDockApps,
  getRecentlyUsedApps,
  toggleAppFavourite,
  reorderFavouriteApps,
  recordAppLaunch,
  evaluateLauncherModeStatus,
  type TVAppItem,
  type SortOrder,
  type OperatingMode,
  type DeviceFirmwareProfile,
} from './utils/launcherLogic';
import {
  LiquidGlassAppCard,
  renderAppIconGlyph,
} from './components/LiquidGlassAppCard';
import { LauncherModeView } from './components/LauncherModeView';
import { AndroidStudioProjectView } from './components/AndroidStudioProjectView';
import { RemoteControlPanel } from './components/RemoteControlPanel';

type ActiveScreen =
  | 'HOME'
  | 'APPS'
  | 'SEARCH'
  | 'WALLPAPERS'
  | 'LAUNCHER_MODE'
  | 'SETTINGS'
  | 'ANDROID_PROJECT';

type SettingsTab = 'APPEARANCE' | 'LAUNCHER' | 'DISPLAY' | 'ABOUT' | 'RESET';

const REMOTE_KEYS = [
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
];

const ACCENT_PRESETS = [
  { name: 'Icy Blue', hex: '#38BDF8' },
  { name: 'Arctic Cyan', hex: '#22D3EE' },
  { name: 'Frost Indigo', hex: '#818CF8' },
  { name: 'Emerald Glass', hex: '#34D399' },
  { name: 'Amber Glow', hex: '#FBBF24' },
];

const STORAGE_KEY = 'shanawaz_tv_launcher_state_v1';

export default function App() {
  // Persistent state initialization
  const [apps, setApps] = useState<TVAppItem[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed.apps) && parsed.apps.length > 0) {
          return parsed.apps;
        }
      }
    } catch {
      // Fallback to initial apps
    }
    return INITIAL_TV_APPS;
  });

  const [activeScreen, setActiveScreen] = useState<ActiveScreen>('HOME');
  const [settingsTab, setSettingsTab] = useState<SettingsTab>('APPEARANCE');

  // Appearance & Display Preferences
  const [themeMode, setThemeMode] = useState<'DARK' | 'LIGHT' | 'AUTO'>('DARK');
  const [glassIntensity, setGlassIntensity] = useState<number>(0.78);
  const [accentHex, setAccentHex] = useState<string>('#38BDF8');
  const [reducedMotion, setReducedMotion] = useState<boolean>(false);
  const [lowPerfMode, setLowPerfMode] = useState<boolean>(false);
  const [uiScale, setUiScale] = useState<number>(1.0);
  const [safeAreaPadding, setSafeAreaPadding] = useState<number>(28);

  // Dual Mode (Mode 1: Default Home Launcher vs Mode 2: Normal Application)
  const [operatingMode, setOperatingMode] = useState<OperatingMode>('DEFAULT_LAUNCHER');
  const [firmwareProfile, setFirmwareProfile] =
    useState<DeviceFirmwareProfile>('STANDARD_ANDROID_TV');
  const [userGrantedSystemDefaultHome, setUserGrantedSystemDefaultHome] =
    useState<boolean>(true);

  // Wallpaper & Promotional Hero State
  const [customWallpapers, setCustomWallpapers] = useState<WallpaperOption[]>([]);
  const [activeWallpaperId, setActiveWallpaperId] = useState<string>(
    'liquid_glass_icy_blue'
  );
  const [previewWallpaperId, setPreviewWallpaperId] = useState<string>(
    'liquid_glass_icy_blue'
  );
  const [wallpaperImgFailed, setWallpaperImgFailed] = useState<Record<string, boolean>>({});
  const [promoTitle, setPromoTitle] = useState<string>('Welcome to TV LAUNCHER');
  const [promoSubtitle, setPromoSubtitle] = useState<string>(
    'Liquid Glass Android TV & Google TV Home Experience by Shanawaz'
  );

  // Sorting, Search & Filtering
  const [sortOrder, setSortOrder] = useState<SortOrder>('ALPHABETICAL_ASC');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Clock & Screensaver
  const [use24HourClock, setUse24HourClock] = useState<boolean>(false);
  const [showClockSeconds, setShowClockSeconds] = useState<boolean>(false);
  const [screensaverEnabled, setScreensaverEnabled] = useState<boolean>(true);
  const [screensaverDelayMin, setScreensaverDelayMin] = useState<number>(5);
  const [screensaverActive, setScreensaverActive] = useState<boolean>(false);
  const [nowDate, setNowDate] = useState<Date>(() => new Date());

  // D-Pad Focus & Remote Controller Drawer
  const [focusedPackage, setFocusedPackage] = useState<string>(
    'com.google.android.youtube.tv'
  );
  const [remoteOpen, setRemoteOpen] = useState<boolean>(false);

  // Launch Overlay & Error Banner
  const [launchedAppModal, setLaunchedAppModal] = useState<TVAppItem | null>(null);
  const [launchErrorBanner, setLaunchErrorBanner] = useState<string | null>(null);
  const [confirmFactoryReset, setConfirmFactoryReset] = useState<boolean>(false);

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Save state to localStorage
  useEffect(() => {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          apps,
          themeMode,
          glassIntensity,
          accentHex,
          operatingMode,
          activeWallpaperId,
          sortOrder,
          use24HourClock,
          showClockSeconds,
        })
      );
    } catch {
      // Ignore storage quota errors
    }
  }, [
    apps,
    themeMode,
    glassIntensity,
    accentHex,
    operatingMode,
    activeWallpaperId,
    sortOrder,
    use24HourClock,
    showClockSeconds,
  ]);

  // Sync dark mode class on <html>
  useEffect(() => {
    const root = document.documentElement;
    if (themeMode === 'LIGHT') {
      root.classList.remove('dark');
    } else {
      root.classList.add('dark');
    }
  }, [themeMode]);

  // Live Clock Ticker
  useEffect(() => {
    const timer = setInterval(() => setNowDate(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // Computed Wallpaper List
  const allWallpapers = useMemo(
    () => [...BUILT_IN_WALLPAPERS, ...customWallpapers],
    [customWallpapers]
  );

  const activeWallpaper = useMemo(
    () =>
      allWallpapers.find((w) => w.id === activeWallpaperId) ||
      BUILT_IN_WALLPAPERS[0],
    [allWallpapers, activeWallpaperId]
  );

  const previewWallpaper = useMemo(
    () =>
      allWallpapers.find((w) => w.id === previewWallpaperId) ||
      activeWallpaper,
    [allWallpapers, previewWallpaperId, activeWallpaper]
  );

  // Computed App Lists
  const visibleApps = useMemo(
    () =>
      filterAndSortApps(apps, {
        sortOrder,
        includeHidden: false,
        categoryFilter,
      }),
    [apps, sortOrder, categoryFilter]
  );

  const favouriteDockApps = useMemo(() => getFavouriteDockApps(apps), [apps]);
  const recentlyUsedApps = useMemo(() => getRecentlyUsedApps(apps, 6), [apps]);
  const hiddenApps = useMemo(() => apps.filter((a) => a.isHidden), [apps]);

  const searchResults = useMemo(
    () =>
      filterAndSortApps(apps, {
        searchQuery,
        sortOrder,
        includeHidden: false,
      }),
    [apps, searchQuery, sortOrder]
  );

  const featuredApp = useMemo(
    () =>
      favouriteDockApps.find((a) => a.packageName !== 'com.shanawaz.tvlauncher') ||
      visibleApps[0] ||
      INITIAL_TV_APPS[1],
    [favouriteDockApps, visibleApps]
  );

  const modeEvaluation = useMemo(
    () =>
      evaluateLauncherModeStatus(
        operatingMode,
        firmwareProfile,
        userGrantedSystemDefaultHome
      ),
    [operatingMode, firmwareProfile, userGrantedSystemDefaultHome]
  );

  // Format Clock & Date
  const formattedTime = useMemo(() => {
    return nowDate.toLocaleTimeString('en-US', {
      hour12: !use24HourClock,
      hour: '2-digit',
      minute: '2-digit',
      ...(showClockSeconds ? { second: '2-digit' } : {}),
    });
  }, [nowDate, use24HourClock, showClockSeconds]);

  const formattedDate = useMemo(() => {
    return nowDate.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    });
  }, [nowDate]);

  // Handlers
  const handleLaunchApp = (app: TVAppItem) => {
    setLaunchErrorBanner(null);

    // Check for missing launch activity (demonstrates error handling requirement)
    if (!app.activityName || app.activityName.trim() === '') {
      setLaunchErrorBanner(
        `Launch Error: Package "${app.appName}" (${app.packageName}) does not expose a valid Leanback or MAIN launch activity.`
      );
      return;
    }

    // Special shortcut for Android TV Settings package
    if (app.packageName === 'com.android.tv.settings') {
      setActiveScreen('SETTINGS');
      return;
    }

    // Record launch timestamp for Recently Used row
    setApps((prev) => recordAppLaunch(prev, app.packageName));
    setLaunchedAppModal(app);
  };

  const handleToggleFavourite = (app: TVAppItem) => {
    setApps((prev) => toggleAppFavourite(prev, app.packageName));
  };

  const handleMoveFavourite = (app: TVAppItem, direction: -1 | 1) => {
    setApps((prev) => reorderFavouriteApps(prev, app.packageName, direction));
  };

  const handleToggleHidden = (app: TVAppItem) => {
    setApps((prev) =>
      prev.map((item) =>
        item.packageName === app.packageName
          ? { ...item, isHidden: !item.isHidden }
          : item
      )
    );
  };

  const handleDpadStep = (dir: 'UP' | 'DOWN' | 'LEFT' | 'RIGHT') => {
    const currentPool =
      activeScreen === 'SEARCH' ? searchResults : visibleApps;
    if (currentPool.length === 0) return;

    const currentIdx = currentPool.findIndex(
      (a) => a.packageName === focusedPackage
    );
    const baseIdx = currentIdx === -1 ? 0 : currentIdx;

    let nextIdx = baseIdx;
    if (dir === 'RIGHT') nextIdx = (baseIdx + 1) % currentPool.length;
    if (dir === 'LEFT')
      nextIdx = (baseIdx - 1 + currentPool.length) % currentPool.length;
    if (dir === 'DOWN') nextIdx = Math.min(currentPool.length - 1, baseIdx + 4);
    if (dir === 'UP') nextIdx = Math.max(0, baseIdx - 4);

    setFocusedPackage(currentPool[nextIdx].packageName);
  };

  const handleDpadSelect = () => {
    const target = apps.find((a) => a.packageName === focusedPackage);
    if (target) {
      handleLaunchApp(target);
    }
  };

  // Global Keyboard D-Pad Navigation Listener
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (screensaverActive) {
        setScreensaverActive(false);
        return;
      }
      const tag = (e.target as HTMLElement)?.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;

      if (e.key === 'ArrowRight') {
        e.preventDefault();
        handleDpadStep('RIGHT');
      } else if (e.key === 'ArrowLeft') {
        e.preventDefault();
        handleDpadStep('LEFT');
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        handleDpadStep('DOWN');
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        handleDpadStep('UP');
      } else if (e.key === 'Escape') {
        if (launchedAppModal) {
          setLaunchedAppModal(null);
        } else if (activeScreen !== 'HOME') {
          setActiveScreen('HOME');
        }
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  });

  // Handle Custom SAF Wallpaper Upload
  const handleCustomWallpaperUpload = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setLaunchErrorBanner(
        'Wallpaper Error: Selected file is not a valid television image format.'
      );
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      const customId = `custom_saf_${Date.now()}`;
      const newPreset: WallpaperOption = {
        id: customId,
        title: file.name.replace(/\.[^/.]+$/, ''),
        subtitle: 'Custom User Image selected via Storage Access Framework',
        category: 'Custom',
        imageUrl: dataUrl,
        fallbackGradient: 'linear-gradient(135deg, #080B14 0%, #1E293B 100%)',
        accentSuggestion: '#38BDF8',
      };
      setCustomWallpapers((prev) => [newPreset, ...prev]);
      setPreviewWallpaperId(customId);
      setActiveWallpaperId(customId);
    };
    reader.readAsDataURL(file);
  };

  const handleResetAppearance = () => {
    setThemeMode('DARK');
    setGlassIntensity(0.78);
    setAccentHex('#38BDF8');
    setActiveWallpaperId('liquid_glass_icy_blue');
    setPreviewWallpaperId('liquid_glass_icy_blue');
  };

  const handleResetFavourites = () => {
    setApps((prev) =>
      prev.map((app, idx) => ({
        ...app,
        isFavourite: idx < 6,
        favouriteOrder: idx < 6 ? idx : 999,
      }))
    );
  };

  const handleFullFactoryReset = () => {
    localStorage.removeItem(STORAGE_KEY);
    setApps(INITIAL_TV_APPS);
    handleResetAppearance();
    setOperatingMode('DEFAULT_LAUNCHER');
    setFirmwareProfile('STANDARD_ANDROID_TV');
    setUserGrantedSystemDefaultHome(true);
    setSortOrder('ALPHABETICAL_ASC');
    setUse24HourClock(false);
    setShowClockSeconds(false);
    setConfirmFactoryReset(false);
  };

  return (
    <div
      className={`min-h-screen relative select-none transition-colors ${
        themeMode === 'LIGHT'
          ? 'bg-slate-900 text-white'
          : 'bg-[#080B14] text-white'
      }`}
    >
      {/* Dynamic Television Wallpaper Backdrop + Measured Contrast Scrim */}
      <div className="fixed inset-0 z-0 overflow-hidden pointer-events-none">
        {!wallpaperImgFailed[activeWallpaper.id] ? (
          <img
            src={activeWallpaper.imageUrl}
            alt={activeWallpaper.title}
            referrerPolicy="no-referrer"
            onError={() =>
              setWallpaperImgFailed((prev) => ({
                ...prev,
                [activeWallpaper.id]: true,
              }))
            }
            className="w-full h-full object-cover opacity-55 scale-105 transition-all duration-700"
          />
        ) : (
          <div
            className="w-full h-full"
            style={{ background: activeWallpaper.fallbackGradient }}
          />
        )}
        <div
          className="absolute inset-0"
          style={{
            background:
              themeMode === 'LIGHT'
                ? 'linear-gradient(180deg, rgba(15,23,42,0.58) 0%, rgba(15,23,42,0.82) 100%)'
                : 'linear-gradient(180deg, rgba(8,11,20,0.68) 0%, rgba(8,11,20,0.88) 65%, rgba(8,11,20,0.96) 100%)',
          }}
        />
      </div>

      {/* Main Safe-Area Television Viewport Container */}
      <div
        className="relative z-10 max-w-[1440px] mx-auto min-h-screen flex flex-col"
        style={{
          padding: `${Math.round(safeAreaPadding * 0.75)}px ${safeAreaPadding}px`,
          transform: uiScale !== 1 ? `scale(${uiScale})` : undefined,
          transformOrigin: 'top center',
        }}
      >
        {/* TOP NAVIGATION BAR — Strict 3-Zone Contract */}
        <header
          className="flex items-center justify-between gap-8 px-6 py-3.5 rounded-[22px] border border-white/15"
          style={{
            background: `linear-gradient(145deg, rgba(255,255,255,${glassIntensity * 0.14}) 0%, rgba(17,24,39,0.78) 100%)`,
            backdropFilter: `blur(${Math.round(glassIntensity * 20)}px)`,
          }}
        >
          {/* Zone 1: Single-line Brand Wordmark */}
          <a
            href="#home"
            onClick={(e) => {
              e.preventDefault();
              setActiveScreen('HOME');
            }}
            className="text-lg font-display font-bold tracking-tight text-white whitespace-nowrap shrink-0"
          >
            TV LAUNCHER
          </a>

          {/* Zone 2: Concise Single-Line Text Navigation Links */}
          <nav
            aria-label="Primary Television Navigation"
            className="flex items-center gap-6 text-sm font-medium text-[#AAB4C8] overflow-x-auto"
          >
            {(
              [
                { id: 'HOME', label: 'Home' },
                { id: 'APPS', label: 'All Apps' },
                { id: 'SEARCH', label: 'Search' },
                { id: 'WALLPAPERS', label: 'Wallpapers' },
                { id: 'LAUNCHER_MODE', label: 'Launcher Mode' },
                { id: 'SETTINGS', label: 'Settings' },
                { id: 'ANDROID_PROJECT', label: 'Android Studio APK' },
              ] as Array<{ id: ActiveScreen; label: string }>
            ).map((item) => {
              const isCurrent = activeScreen === item.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setActiveScreen(item.id)}
                  className={`py-1 transition-colors whitespace-nowrap shrink-0 border-b-2 ${
                    isCurrent
                      ? 'text-white border-sky-400 font-semibold'
                      : 'border-transparent hover:text-white'
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </nav>

          {/* Zone 3: 1 Primary Action Button */}
          <div className="flex items-center gap-3 shrink-0">
            <button
              type="button"
              onClick={() => setRemoteOpen((prev) => !prev)}
              style={{ backgroundColor: accentHex }}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-slate-950 rounded-xl hover:opacity-95 transition-opacity whitespace-nowrap shrink-0"
            >
              <Gamepad2 className="w-4 h-4" />
              <span>{remoteOpen ? 'Hide TV Remote' : 'TV Remote'}</span>
            </button>
          </div>
        </header>

        {/* System Status Sub-Bar (Time, Date, Wi-Fi, Battery, Dual Mode Status) */}
        <div className="mt-3 px-2 flex flex-wrap items-center justify-between gap-4 text-xs text-[#AAB4C8] font-mono tabular-nums">
          <div className="flex items-center gap-2">
            <span className="text-white font-medium">
              {modeEvaluation.effectiveMode === 'DEFAULT_LAUNCHER'
                ? 'Mode 1: Default Home Launcher'
                : 'Mode 2: Normal Application'}
            </span>
            <span aria-hidden="true">·</span>
            <span>Package: com.shanawaz.tvlauncher</span>
            <span aria-hidden="true">·</span>
            <span>Developer: Shanawaz</span>
          </div>

          <div className="flex items-center gap-3">
            <span className="inline-flex items-center gap-1.5 text-white">
              <Wifi className="w-3.5 h-3.5 text-sky-400" />
              <span>Wi-Fi 5GHz Connected</span>
            </span>
            <span aria-hidden="true">·</span>
            <span className="inline-flex items-center gap-1">
              <BatteryCharging className="w-3.5 h-3.5 text-emerald-400" />
              <span>Remote 94%</span>
            </span>
            <span aria-hidden="true">·</span>
            <span>{formattedDate}</span>
            <span aria-hidden="true">·</span>
            <span className="text-white font-semibold">{formattedTime}</span>
          </div>
        </div>

        {/* Error Notification Banner (when missing launch activity or invalid file occurs) */}
        {launchErrorBanner && (
          <div
            role="alert"
            className="mt-4 px-5 py-3.5 rounded-2xl bg-red-950/85 border border-red-400/50 text-sm text-white flex items-center justify-between gap-4"
          >
            <div className="flex items-center gap-2.5">
              <AlertTriangle className="w-5 h-5 text-red-400 shrink-0" />
              <span>{launchErrorBanner}</span>
            </div>
            <button
              type="button"
              onClick={() => setLaunchErrorBanner(null)}
              className="px-3 py-1 rounded-lg bg-white/10 hover:bg-white/20 text-xs font-medium whitespace-nowrap"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* MAIN TELEVISION VIEWPORT */}
        <main className="mt-6 flex-1 space-y-8 pb-12">
          {/* SCREEN 1: HOME DASHBOARD */}
          {activeScreen === 'HOME' && (
            <>
              {/* Welcome Hero & Featured Application Spotlight (Balanced 2-Column Split) */}
              <section className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
                {/* Left: Welcome & Promotional Liquid Glass Hero */}
                <div
                  className="lg:col-span-8 rounded-[26px] p-7 flex flex-col justify-between relative overflow-hidden border border-white/15"
                  style={{
                    background: `linear-gradient(135deg, rgba(255,255,255,${glassIntensity * 0.16}) 0%, rgba(17,24,39,0.82) 100%)`,
                    backdropFilter: `blur(${Math.round(glassIntensity * 22)}px)`,
                  }}
                >
                  <div className="space-y-2.5 max-w-2xl">
                    <div className="text-xs font-mono text-sky-300 tracking-wide">
                      {formattedDate.toUpperCase()} · {formattedTime} ·{' '}
                      {activeWallpaper.title.toUpperCase()}
                    </div>
                    <h1
                      className="text-3xl md:text-4xl font-display font-bold text-white tracking-tight"
                      style={{ textWrap: 'balance' }}
                    >
                      {promoTitle}
                    </h1>
                    <p className="text-sm md:text-base text-[#AAB4C8] leading-relaxed">
                      {promoSubtitle}
                    </p>
                  </div>

                  <div className="mt-6 pt-5 border-t border-white/10 flex flex-wrap items-center justify-between gap-4">
                    <div className="flex flex-wrap items-center gap-3">
                      <button
                        type="button"
                        onClick={() => setActiveScreen('APPS')}
                        style={{ backgroundColor: accentHex }}
                        className="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-950 hover:opacity-95 transition-opacity whitespace-nowrap shrink-0"
                      >
                        Browse All Applications ({visibleApps.length})
                      </button>
                      <button
                        type="button"
                        onClick={() => setActiveScreen('LAUNCHER_MODE')}
                        className="px-4 py-2.5 rounded-xl text-sm font-medium text-white bg-white/10 border border-white/15 hover:bg-white/15 transition-colors whitespace-nowrap shrink-0"
                      >
                        Launcher Mode ({modeEvaluation.effectiveMode === 'DEFAULT_LAUNCHER' ? 'Default Home' : 'Normal App'})
                      </button>
                      <button
                        type="button"
                        onClick={() => setActiveScreen('ANDROID_PROJECT')}
                        className="px-4 py-2.5 rounded-xl text-sm font-medium text-white bg-white/10 border border-white/15 hover:bg-white/15 transition-colors whitespace-nowrap shrink-0"
                      >
                        Inspect Native Kotlin & APK
                      </button>
                    </div>

                    <span className="text-xs font-mono text-[#AAB4C8] tabular-nums">
                      {favouriteDockApps.length} Pinned · {recentlyUsedApps.length} Recent
                    </span>
                  </div>
                </div>

                {/* Right: Featured Application Card */}
                <div
                  className="lg:col-span-4 rounded-[26px] p-7 flex flex-col justify-between border border-white/15"
                  style={{
                    background: `linear-gradient(145deg, rgba(255,255,255,${glassIntensity * 0.14}) 0%, rgba(17,24,39,0.86) 100%)`,
                    backdropFilter: `blur(${Math.round(glassIntensity * 20)}px)`,
                  }}
                >
                  <div>
                    <div className="text-xs font-mono text-[#AAB4C8]">
                      FEATURED TELEVISION APP
                    </div>
                    <div className="mt-4 flex items-center gap-4">
                      <div
                        className="w-16 h-16 rounded-2xl flex items-center justify-center shrink-0 shadow-lg"
                        style={{
                          background: `linear-gradient(135deg, ${featuredApp.brandColor} 0%, ${featuredApp.accentColor} 100%)`,
                          border: '1px solid rgba(255,255,255,0.3)',
                        }}
                      >
                        {renderAppIconGlyph(featuredApp.iconCode, 'w-8 h-8 text-white')}
                      </div>
                      <div className="min-w-0">
                        <h2 className="text-xl font-display font-bold text-white truncate">
                          {featuredApp.appName}
                        </h2>
                        <p className="text-xs font-mono text-[#AAB4C8] truncate">
                          {featuredApp.packageName}
                        </p>
                        <p className="text-xs text-sky-300 mt-1">
                          {featuredApp.category} · v{featuredApp.versionName}
                        </p>
                      </div>
                    </div>
                    <p className="mt-4 text-sm text-[#AAB4C8] leading-relaxed">
                      {featuredApp.description}
                    </p>
                  </div>

                  <div className="mt-6 pt-4 border-t border-white/10 flex items-center justify-between gap-3">
                    <button
                      type="button"
                      onClick={() => handleLaunchApp(featuredApp)}
                      style={{ backgroundColor: accentHex }}
                      className="w-full py-2.5 px-4 rounded-xl text-sm font-semibold text-slate-950 hover:opacity-95 transition-opacity whitespace-nowrap"
                    >
                      Open {featuredApp.appName}
                    </button>
                  </div>
                </div>
              </section>

              {/* LIQUID GLASS FAVOURITE APPLICATIONS DOCK */}
              <section aria-label="Favourite Applications Dock" className="space-y-3">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <h2 className="text-xl font-display font-semibold text-white">
                      Favourite Applications Dock
                    </h2>
                    <p className="text-xs text-[#AAB4C8]">
                      Horizontal Liquid Glass dock · Reorder apps left/right, pin or unpin, and launch with D-pad OK
                    </p>
                  </div>
                  <span className="text-xs font-mono text-[#AAB4C8] tabular-nums">
                    {favouriteDockApps.length} Favourites Saved
                  </span>
                </div>

                <div
                  className="rounded-[28px] p-5 border border-white/15"
                  style={{
                    background: `linear-gradient(180deg, rgba(255,255,255,${glassIntensity * 0.12}) 0%, rgba(17,24,39,0.75) 100%)`,
                    backdropFilter: `blur(${Math.round(glassIntensity * 22)}px)`,
                  }}
                >
                  {favouriteDockApps.length === 0 ? (
                    <div className="py-8 text-center space-y-2">
                      <p className="text-base font-display font-medium text-white">
                        Your Liquid Glass Dock is empty
                      </p>
                      <p className="text-xs text-[#AAB4C8]">
                        Click &ldquo;Pin&rdquo; on any installed application below to add it to your Favourites Dock.
                      </p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
                      {favouriteDockApps.map((app) => (
                        <LiquidGlassAppCard
                          key={`dock_${app.packageName}`}
                          app={app}
                          compact
                          showDockReorder
                          isFocused={focusedPackage === app.packageName}
                          accentHex={accentHex}
                          glassIntensity={glassIntensity}
                          reducedMotion={reducedMotion}
                          onFocusCard={() => setFocusedPackage(app.packageName)}
                          onLaunch={handleLaunchApp}
                          onToggleFavourite={handleToggleFavourite}
                          onMoveFavourite={handleMoveFavourite}
                        />
                      ))}
                    </div>
                  )}
                </div>
              </section>

              {/* RECENTLY USED APPLICATIONS */}
              {recentlyUsedApps.length > 0 && (
                <section aria-label="Recently Used Applications" className="space-y-3">
                  <div className="flex items-center justify-between gap-4">
                    <div>
                      <h2 className="text-xl font-display font-semibold text-white">
                        Recently Used Applications
                      </h2>
                      <p className="text-xs text-[#AAB4C8]">
                        Recorded from local TV LAUNCHER session activity (respecting Android usage-stats privacy)
                      </p>
                    </div>
                    <span className="text-xs font-mono text-[#AAB4C8] tabular-nums">
                      {recentlyUsedApps.length} Recent
                    </span>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
                    {recentlyUsedApps.map((app) => (
                      <LiquidGlassAppCard
                        key={`recent_${app.packageName}`}
                        app={app}
                        compact
                        isFocused={focusedPackage === app.packageName}
                        accentHex={accentHex}
                        glassIntensity={glassIntensity}
                        reducedMotion={reducedMotion}
                        onFocusCard={() => setFocusedPackage(app.packageName)}
                        onLaunch={handleLaunchApp}
                        onToggleFavourite={handleToggleFavourite}
                      />
                    ))}
                  </div>
                </section>
              )}

              {/* ALL INSTALLED APPLICATIONS PREVIEW */}
              <section aria-label="Installed TV Applications" className="space-y-3">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <h2 className="text-xl font-display font-semibold text-white">
                      Installed Television Applications
                    </h2>
                    <p className="text-xs text-[#AAB4C8]">
                      Discovered via Android PackageManager (LEANBACK_LAUNCHER & compatible activities)
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setActiveScreen('APPS')}
                    className="px-4 py-2 rounded-xl text-xs font-medium text-white bg-white/10 hover:bg-white/15 border border-white/15 transition-colors whitespace-nowrap shrink-0"
                  >
                    Manage Full App Library ({visibleApps.length})
                  </button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  {visibleApps.slice(0, 8).map((app) => (
                    <LiquidGlassAppCard
                      key={`home_all_${app.packageName}`}
                      app={app}
                      isFocused={focusedPackage === app.packageName}
                      accentHex={accentHex}
                      glassIntensity={glassIntensity}
                      reducedMotion={reducedMotion}
                      showHideControl
                      onFocusCard={() => setFocusedPackage(app.packageName)}
                      onLaunch={handleLaunchApp}
                      onToggleFavourite={handleToggleFavourite}
                      onToggleHidden={handleToggleHidden}
                    />
                  ))}
                </div>
              </section>
            </>
          )}

          {/* SCREEN 2: ALL APPLICATIONS LIBRARY */}
          {activeScreen === 'APPS' && (
            <section className="space-y-6">
              <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
                <div>
                  <p className="text-xs font-mono text-sky-400">
                    PACKAGEMANAGER DISCOVERY · LEANBACK & STANDARD ACTIVITIES
                  </p>
                  <h1 className="text-3xl font-display font-bold text-white mt-1">
                    All Applications Library ({visibleApps.length})
                  </h1>
                  <p className="text-sm text-[#AAB4C8] mt-1">
                    Sort alphabetically, filter by category, pin favourites to your Liquid Glass dock, or hide apps without uninstalling them.
                  </p>
                </div>

                {/* Sort & Category Filter Bar */}
                <div className="flex flex-wrap items-center gap-2.5">
                  <div className="flex items-center gap-1 p-1 rounded-xl bg-[#111827]/90 border border-white/15">
                    {(
                      [
                        { id: 'ALPHABETICAL_ASC', label: 'A–Z' },
                        { id: 'ALPHABETICAL_DESC', label: 'Z–A' },
                        { id: 'RECENTLY_USED', label: 'Recent' },
                        { id: 'CUSTOM_ORDER', label: 'Custom Order' },
                      ] as Array<{ id: SortOrder; label: string }>
                    ).map((s) => (
                      <button
                        key={s.id}
                        type="button"
                        onClick={() => setSortOrder(s.id)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                          sortOrder === s.id
                            ? 'bg-white text-slate-950 font-semibold'
                            : 'text-[#AAB4C8] hover:text-white'
                        }`}
                      >
                        {s.label}
                      </button>
                    ))}
                  </div>

                  <button
                    type="button"
                    onClick={() => setApps([...apps])}
                    className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-medium text-white bg-white/10 hover:bg-white/15 border border-white/15 transition-colors whitespace-nowrap shrink-0"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    <span>Refresh App List</span>
                  </button>
                </div>
              </div>

              {/* Category Filter Tabs */}
              <div className="flex items-center gap-2 overflow-x-auto pb-1">
                {['ALL', 'Streaming', 'Media & Plex', 'Gaming', 'Utilities', 'System'].map(
                  (cat) => (
                    <button
                      key={cat}
                      type="button"
                      onClick={() => setCategoryFilter(cat)}
                      className={`px-3.5 py-1.5 rounded-xl text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                        categoryFilter === cat
                          ? 'bg-sky-400 text-slate-950 font-semibold'
                          : 'bg-[#111827]/80 text-[#AAB4C8] hover:text-white border border-white/10'
                      }`}
                    >
                      {cat === 'ALL' ? 'All Categories' : cat}
                    </button>
                  )
                )}
              </div>

              {/* Applications Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {visibleApps.map((app) => (
                  <LiquidGlassAppCard
                    key={`lib_${app.packageName}`}
                    app={app}
                    isFocused={focusedPackage === app.packageName}
                    accentHex={accentHex}
                    glassIntensity={glassIntensity}
                    reducedMotion={reducedMotion}
                    showHideControl
                    onFocusCard={() => setFocusedPackage(app.packageName)}
                    onLaunch={handleLaunchApp}
                    onToggleFavourite={handleToggleFavourite}
                    onToggleHidden={handleToggleHidden}
                  />
                ))}
              </div>

              {/* Hidden Applications Tray */}
              {hiddenApps.length > 0 && (
                <div className="rounded-[24px] p-5 bg-[#111827]/85 border border-white/15 space-y-3">
                  <div className="flex items-center justify-between">
                    <h3 className="text-base font-display font-semibold text-white">
                      Hidden from Launcher Display ({hiddenApps.length})
                    </h3>
                    <span className="text-xs text-[#AAB4C8]">
                      Hidden apps remain installed on your TV
                    </span>
                  </div>
                  <div className="flex flex-wrap gap-2.5">
                    {hiddenApps.map((hApp) => (
                      <button
                        key={hApp.packageName}
                        type="button"
                        onClick={() => handleToggleHidden(hApp)}
                        className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-xs text-white font-medium"
                      >
                        <Eye className="w-3.5 h-3.5 text-sky-400" />
                        <span>Unhide {hApp.appName}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </section>
          )}

          {/* SCREEN 3: REMOTE-FRIENDLY SEARCH */}
          {activeScreen === 'SEARCH' && (
            <section className="space-y-6">
              <div>
                <p className="text-xs font-mono text-sky-400">
                  LOCAL PACKAGE SEARCH · D-PAD ON-SCREEN KEYBOARD
                </p>
                <h1 className="text-3xl font-display font-bold text-white mt-1">
                  Search Installed Applications
                </h1>
              </div>

              {/* Search Input Bar */}
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                <div className="relative flex-1">
                  <SearchIcon className="w-5 h-5 text-[#AAB4C8] absolute left-4 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search installed TV apps by name, category, or package ID..."
                    aria-label="Search installed applications"
                    className="w-full pl-12 pr-4 py-3.5 rounded-2xl bg-[#111827]/90 border border-white/20 text-white placeholder-[#AAB4C8] text-sm focus:outline-none focus:border-sky-400"
                  />
                </div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setSearchQuery((q) => q.slice(0, -1))}
                    className="px-4 py-3.5 rounded-2xl bg-white/10 hover:bg-white/15 text-xs font-medium text-white whitespace-nowrap"
                  >
                    Backspace
                  </button>
                  <button
                    type="button"
                    onClick={() => setSearchQuery('')}
                    className="px-4 py-3.5 rounded-2xl bg-white/10 hover:bg-white/15 text-xs font-medium text-white whitespace-nowrap"
                  >
                    Clear Search
                  </button>
                </div>
              </div>

              {/* Remote-Friendly On-Screen Keyboard */}
              <div className="rounded-[22px] p-4 bg-[#111827]/75 border border-white/15">
                <div className="flex items-center justify-between text-[11px] font-mono text-[#AAB4C8] mb-2.5">
                  <span>REMOTE D-PAD KEYBOARD (36 ALPHANUMERIC KEYS + SPACE + DEL)</span>
                  <span>Instant Client-Side Package Filter</span>
                </div>
                <div className="grid grid-cols-10 sm:grid-cols-13 md:grid-cols-19 gap-1.5">
                  {REMOTE_KEYS.map((keyChar) => (
                    <button
                      key={keyChar}
                      type="button"
                      onClick={() =>
                        setSearchQuery((prev) => prev + keyChar.toLowerCase())
                      }
                      className="h-9 rounded-xl bg-white/10 hover:bg-sky-400 hover:text-slate-950 text-xs font-mono font-semibold text-white transition-colors"
                    >
                      {keyChar}
                    </button>
                  ))}
                  <button
                    type="button"
                    onClick={() => setSearchQuery((prev) => prev + ' ')}
                    className="h-9 rounded-xl bg-white/15 hover:bg-sky-400 hover:text-slate-950 text-[11px] font-mono font-semibold text-sky-300 transition-colors"
                  >
                    SPC
                  </button>
                  <button
                    type="button"
                    onClick={() => setSearchQuery((prev) => prev.slice(0, -1))}
                    className="h-9 rounded-xl bg-white/15 hover:bg-sky-400 hover:text-slate-950 text-[11px] font-mono font-semibold text-amber-300 transition-colors"
                  >
                    DEL
                  </button>
                </div>
              </div>

              {/* Search Results or Clear Empty State */}
              {searchResults.length === 0 ? (
                <div className="rounded-[24px] p-10 bg-[#111827]/80 border border-white/15 text-center space-y-3">
                  <p className="text-xl font-display font-semibold text-white">
                    No matching applications found for &ldquo;{searchQuery}&rdquo;
                  </p>
                  <p className="text-sm text-[#AAB4C8]">
                    Try searching for another installed TV app or clear the current filter.
                  </p>
                  <button
                    type="button"
                    onClick={() => setSearchQuery('')}
                    style={{ backgroundColor: accentHex }}
                    className="mt-2 px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-950"
                  >
                    Reset Search Filter
                  </button>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  {searchResults.map((app) => (
                    <LiquidGlassAppCard
                      key={`search_${app.packageName}`}
                      app={app}
                      isFocused={focusedPackage === app.packageName}
                      accentHex={accentHex}
                      glassIntensity={glassIntensity}
                      reducedMotion={reducedMotion}
                      onFocusCard={() => setFocusedPackage(app.packageName)}
                      onLaunch={handleLaunchApp}
                      onToggleFavourite={handleToggleFavourite}
                    />
                  ))}
                </div>
              )}
            </section>
          )}

          {/* SCREEN 4: WALLPAPER CUSTOMISATION STUDIO */}
          {activeScreen === 'WALLPAPERS' && (
            <section className="space-y-6">
              <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                  <p className="text-xs font-mono text-sky-400">
                    4K TELEVISION BACKDROPS · STORAGE ACCESS FRAMEWORK (SAF)
                  </p>
                  <h1 className="text-3xl font-display font-bold text-white mt-1">
                    Wallpaper Customisation System
                  </h1>
                  <p className="text-sm text-[#AAB4C8] mt-1">
                    Preview built-in Liquid Glass, Dark OLED, and Cinematic wallpapers before applying, or select a custom image from storage.
                  </p>
                </div>

                <div className="flex flex-wrap items-center gap-3">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleCustomWallpaperUpload}
                    className="hidden"
                  />
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-medium text-white bg-white/10 hover:bg-white/15 border border-white/15 transition-colors whitespace-nowrap shrink-0"
                  >
                    <Upload className="w-4 h-4" />
                    <span>Select Custom Image (SAF)</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setPreviewWallpaperId('liquid_glass_icy_blue');
                      setActiveWallpaperId('liquid_glass_icy_blue');
                    }}
                    className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-medium text-white bg-white/10 hover:bg-white/15 border border-white/15 transition-colors whitespace-nowrap shrink-0"
                  >
                    <RotateCcw className="w-4 h-4" />
                    <span>Restore Default Wallpaper</span>
                  </button>
                </div>
              </div>

              {/* Live 16:9 Television Wallpaper Preview Stage */}
              <div className="rounded-[26px] p-6 bg-[#111827]/85 border border-white/15 space-y-4">
                <div className="relative h-64 md:h-80 w-full rounded-[20px] overflow-hidden border border-white/20">
                  {!wallpaperImgFailed[previewWallpaper.id] ? (
                    <img
                      src={previewWallpaper.imageUrl}
                      alt={previewWallpaper.title}
                      referrerPolicy="no-referrer"
                      onError={() =>
                        setWallpaperImgFailed((prev) => ({
                          ...prev,
                          [previewWallpaper.id]: true,
                        }))
                      }
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div
                      className="w-full h-full"
                      style={{ background: previewWallpaper.fallbackGradient }}
                    />
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/35 to-transparent p-6 flex flex-col justify-end">
                    <span className="text-xs font-mono text-sky-300">
                      LIVE 16:9 TELEVISION PREVIEW · {previewWallpaper.category.toUpperCase()}
                    </span>
                    <h2 className="text-2xl font-display font-bold text-white mt-1">
                      {previewWallpaper.title}
                    </h2>
                    <p className="text-sm text-[#AAB4C8]">
                      {previewWallpaper.subtitle}
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div className="text-sm text-[#AAB4C8]">
                    {activeWallpaperId === previewWallpaper.id ? (
                      <span className="inline-flex items-center gap-2 text-emerald-400 font-medium">
                        <CheckCircle2 className="w-4 h-4" />
                        <span>Currently applied to TV LAUNCHER Home screen</span>
                      </span>
                    ) : (
                      <span>
                        Previewing <strong>{previewWallpaper.title}</strong> — click Apply Wallpaper to save permanently.
                      </span>
                    )}
                  </div>

                  <button
                    type="button"
                    onClick={() => setActiveWallpaperId(previewWallpaper.id)}
                    style={{ backgroundColor: accentHex }}
                    className="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-950 hover:opacity-95 transition-opacity whitespace-nowrap"
                  >
                    {activeWallpaperId === previewWallpaper.id
                      ? 'Applied Wallpaper'
                      : `Apply ${previewWallpaper.title}`}
                  </button>
                </div>
              </div>

              {/* Wallpaper Preset Cards */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                {allWallpapers.map((wp) => {
                  const isPreviewing = previewWallpaper.id === wp.id;
                  const isApplied = activeWallpaperId === wp.id;
                  return (
                    <div
                      key={wp.id}
                      className={`rounded-[22px] overflow-hidden border transition-all ${
                        isPreviewing
                          ? 'border-sky-400 bg-[#111827]'
                          : 'border-white/15 bg-[#111827]/70'
                      }`}
                    >
                      <div className="h-36 relative overflow-hidden">
                        {!wallpaperImgFailed[wp.id] ? (
                          <img
                            src={wp.imageUrl}
                            alt={wp.title}
                            referrerPolicy="no-referrer"
                            onError={() =>
                              setWallpaperImgFailed((prev) => ({
                                ...prev,
                                [wp.id]: true,
                              }))
                            }
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div
                            className="w-full h-full"
                            style={{ background: wp.fallbackGradient }}
                          />
                        )}
                      </div>
                      <div className="p-4 space-y-3">
                        <div>
                          <div className="text-[11px] font-mono text-sky-300">
                            {wp.category} {isApplied ? '· ACTIVE' : ''}
                          </div>
                          <h3 className="text-base font-display font-semibold text-white mt-0.5">
                            {wp.title}
                          </h3>
                          <p className="text-xs text-[#AAB4C8] line-clamp-2 mt-1">
                            {wp.subtitle}
                          </p>
                        </div>

                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => setPreviewWallpaperId(wp.id)}
                            className="flex-1 py-2 px-3 rounded-xl text-xs font-medium text-white bg-white/10 hover:bg-white/15 transition-colors"
                          >
                            Preview
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setPreviewWallpaperId(wp.id);
                              setActiveWallpaperId(wp.id);
                            }}
                            style={{
                              backgroundColor: isApplied ? undefined : accentHex,
                            }}
                            className={`flex-1 py-2 px-3 rounded-xl text-xs font-semibold transition-colors ${
                              isApplied
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-400/40'
                                : 'text-slate-950 hover:opacity-95'
                            }`}
                          >
                            {isApplied ? 'Active' : 'Apply'}
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </section>
          )}

          {/* SCREEN 5: DUAL MODE — LAUNCHER MODE SELECTION */}
          {activeScreen === 'LAUNCHER_MODE' && (
            <LauncherModeView
              selectedMode={operatingMode}
              firmwareProfile={firmwareProfile}
              userGrantedSystemDefaultHome={userGrantedSystemDefaultHome}
              accentHex={accentHex}
              glassIntensity={glassIntensity}
              onSelectMode={setOperatingMode}
              onChangeFirmwareProfile={setFirmwareProfile}
              onSetDefaultHomeGranted={setUserGrantedSystemDefaultHome}
              onReturnToDashboard={() => setActiveScreen('HOME')}
            />
          )}

          {/* SCREEN 6: SETTINGS DASHBOARD */}
          {activeScreen === 'SETTINGS' && (
            <section className="space-y-6">
              <div>
                <p className="text-xs font-mono text-sky-400">
                  PREFERENCES DATASTORE · LOCAL OFFLINE PERSISTENCE
                </p>
                <h1 className="text-3xl font-display font-bold text-white mt-1">
                  Settings Dashboard
                </h1>
              </div>

              {/* Sub-navigation Tabs */}
              <div className="flex items-center gap-2 overflow-x-auto pb-1">
                {(
                  [
                    { id: 'APPEARANCE', label: 'Appearance & Liquid Glass' },
                    { id: 'LAUNCHER', label: 'Launcher & Clock' },
                    { id: 'DISPLAY', label: 'Display & Performance' },
                    { id: 'ABOUT', label: 'About TV LAUNCHER' },
                    { id: 'RESET', label: 'Reset Options' },
                  ] as Array<{ id: SettingsTab; label: string }>
                ).map((tab) => (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setSettingsTab(tab.id)}
                    className={`px-4 py-2 rounded-xl text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                      settingsTab === tab.id
                        ? 'bg-white text-slate-950 font-semibold'
                        : 'bg-[#111827]/85 text-[#AAB4C8] hover:text-white border border-white/15'
                    }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              {/* Tab 1: Appearance */}
              {settingsTab === 'APPEARANCE' && (
                <div className="rounded-[24px] p-6 bg-[#111827]/85 border border-white/15 space-y-6">
                  <div>
                    <h2 className="text-lg font-display font-semibold text-white">
                      Theme Mode
                    </h2>
                    <div className="mt-3 flex flex-wrap gap-3">
                      {(['DARK', 'LIGHT', 'AUTO'] as const).map((m) => (
                        <button
                          key={m}
                          type="button"
                          onClick={() => setThemeMode(m)}
                          className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-colors ${
                            themeMode === m
                              ? 'bg-sky-400 text-slate-950'
                              : 'bg-white/10 text-white hover:bg-white/15'
                          }`}
                        >
                          {m} Theme
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <label
                      htmlFor="glass-intensity-range"
                      className="text-lg font-display font-semibold text-white block"
                    >
                      Liquid Glass Refraction Intensity ({Math.round(glassIntensity * 100)}%)
                    </label>
                    <input
                      id="glass-intensity-range"
                      type="range"
                      min={0.35}
                      max={1.0}
                      step={0.05}
                      value={glassIntensity}
                      onChange={(e) => setGlassIntensity(parseFloat(e.target.value))}
                      className="mt-3 w-full max-w-md accent-sky-400"
                    />
                  </div>

                  <div>
                    <h2 className="text-lg font-display font-semibold text-white">
                      Accent Colour
                    </h2>
                    <div className="mt-3 flex flex-wrap gap-3">
                      {ACCENT_PRESETS.map((preset) => (
                        <button
                          key={preset.hex}
                          type="button"
                          onClick={() => setAccentHex(preset.hex)}
                          className={`inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-medium border transition-colors ${
                            accentHex === preset.hex
                              ? 'border-white bg-white/15 text-white font-semibold'
                              : 'border-white/15 bg-white/5 text-[#AAB4C8] hover:text-white'
                          }`}
                        >
                          <span
                            className="w-3.5 h-3.5 rounded-full"
                            style={{ backgroundColor: preset.hex }}
                          />
                          <span>{preset.name}</span>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* Tab 2: Launcher & Clock */}
              {settingsTab === 'LAUNCHER' && (
                <div className="rounded-[24px] p-6 bg-[#111827]/85 border border-white/15 space-y-6">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-white/10">
                    <div>
                      <h2 className="text-lg font-display font-semibold text-white">
                        Dual-Mode Launcher Configuration
                      </h2>
                      <p className="text-xs text-[#AAB4C8] mt-1">
                        Switch between Mode 1 (Default Home Launcher) and Mode 2 (Normal Application)
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => setActiveScreen('LAUNCHER_MODE')}
                      style={{ backgroundColor: accentHex }}
                      className="px-4 py-2.5 rounded-xl text-xs font-semibold text-slate-950 whitespace-nowrap"
                    >
                      Open LAUNCHER MODE Setup
                    </button>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label
                        htmlFor="promo-title-input"
                        className="text-xs font-mono text-[#AAB4C8] block"
                      >
                        Welcome Hero Headline
                      </label>
                      <input
                        id="promo-title-input"
                        type="text"
                        value={promoTitle}
                        onChange={(e) => setPromoTitle(e.target.value)}
                        className="mt-1.5 w-full px-3.5 py-2.5 rounded-xl bg-[#080B14] border border-white/20 text-sm text-white"
                      />
                    </div>
                    <div>
                      <label
                        htmlFor="promo-subtitle-input"
                        className="text-xs font-mono text-[#AAB4C8] block"
                      >
                        Promotional Subtitle
                      </label>
                      <input
                        id="promo-subtitle-input"
                        type="text"
                        value={promoSubtitle}
                        onChange={(e) => setPromoSubtitle(e.target.value)}
                        className="mt-1.5 w-full px-3.5 py-2.5 rounded-xl bg-[#080B14] border border-white/20 text-sm text-white"
                      />
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-3 pt-2">
                    <button
                      type="button"
                      onClick={() => setUse24HourClock((v) => !v)}
                      className="px-4 py-2.5 rounded-xl text-xs font-medium bg-white/10 hover:bg-white/15 text-white"
                    >
                      Clock Format: {use24HourClock ? '24-Hour' : '12-Hour'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setShowClockSeconds((v) => !v)}
                      className="px-4 py-2.5 rounded-xl text-xs font-medium bg-white/10 hover:bg-white/15 text-white"
                    >
                      Seconds: {showClockSeconds ? 'Visible' : 'Hidden'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setScreensaverEnabled((v) => !v)}
                      className="px-4 py-2.5 rounded-xl text-xs font-medium bg-white/10 hover:bg-white/15 text-white"
                    >
                      Screensaver: {screensaverEnabled ? `Active (${screensaverDelayMin}m)` : 'Disabled'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setScreensaverActive(true)}
                      className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-xs font-medium bg-sky-500/20 border border-sky-400/40 text-sky-300"
                    >
                      <Sparkles className="w-3.5 h-3.5" />
                      <span>Preview Clock Screensaver Now</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Tab 3: Display & Performance */}
              {settingsTab === 'DISPLAY' && (
                <div className="rounded-[24px] p-6 bg-[#111827]/85 border border-white/15 space-y-6">
                  <div>
                    <h2 className="text-lg font-display font-semibold text-white">
                      Television UI Scale & Overscan Safe-Area
                    </h2>
                    <div className="mt-3 flex flex-wrap gap-3">
                      {[0.95, 1.0, 1.05].map((scaleVal) => (
                        <button
                          key={scaleVal}
                          type="button"
                          onClick={() => setUiScale(scaleVal)}
                          className={`px-4 py-2 rounded-xl text-xs font-medium ${
                            uiScale === scaleVal
                              ? 'bg-sky-400 text-slate-950 font-semibold'
                              : 'bg-white/10 text-white'
                          }`}
                        >
                          UI Scale {Math.round(scaleVal * 100)}%
                        </button>
                      ))}
                      {[16, 28, 44].map((padVal) => (
                        <button
                          key={padVal}
                          type="button"
                          onClick={() => setSafeAreaPadding(padVal)}
                          className={`px-4 py-2 rounded-xl text-xs font-medium ${
                            safeAreaPadding === padVal
                              ? 'bg-sky-400 text-slate-950 font-semibold'
                              : 'bg-white/10 text-white'
                          }`}
                        >
                          Overscan Safe Margin: {padVal}px
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <h2 className="text-lg font-display font-semibold text-white">
                      Performance & Low-Memory TV Optimisation
                    </h2>
                    <div className="mt-3 flex flex-wrap gap-3">
                      <button
                        type="button"
                        onClick={() => setReducedMotion((v) => !v)}
                        className={`px-4 py-2.5 rounded-xl text-xs font-medium ${
                          reducedMotion
                            ? 'bg-sky-400 text-slate-950 font-semibold'
                            : 'bg-white/10 text-white'
                        }`}
                      >
                        Reduced Animation Mode: {reducedMotion ? 'ON' : 'OFF'}
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          const next = !lowPerfMode;
                          setLowPerfMode(next);
                          if (next) setGlassIntensity(0.45);
                        }}
                        className={`px-4 py-2.5 rounded-xl text-xs font-medium ${
                          lowPerfMode
                            ? 'bg-sky-400 text-slate-950 font-semibold'
                            : 'bg-white/10 text-white'
                        }`}
                      >
                        Low-Performance Device Mode (Selective GPU Blur):{' '}
                        {lowPerfMode ? 'ON' : 'OFF'}
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Tab 4: About TV LAUNCHER */}
              {settingsTab === 'ABOUT' && (
                <div className="rounded-[24px] p-6 bg-[#111827]/85 border border-white/15 space-y-4">
                  <div className="text-xs font-mono text-sky-400">
                    APPLICATION IDENTITY & RELEASE METADATA
                  </div>
                  <h2 className="text-2xl font-display font-bold text-white">
                    TV LAUNCHER
                  </h2>
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 text-sm">
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">Developer</span>
                      <span className="font-display font-semibold text-white mt-0.5 block">
                        Shanawaz
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">Package Name</span>
                      <span className="font-mono text-white mt-0.5 block">
                        com.shanawaz.tvlauncher
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">Version & Build</span>
                      <span className="font-mono text-white mt-0.5 block">
                        v1.0.0 (Version Code 1)
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">Android Compatibility</span>
                      <span className="text-white mt-0.5 block">
                        Android 6.0 (API 23) — Android 15 (API 35)
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">UI Framework</span>
                      <span className="text-white mt-0.5 block">
                        Kotlin + Jetpack Compose for TV
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-white/5 border border-white/10">
                      <span className="text-xs text-[#AAB4C8] block">Open-Source Licences</span>
                      <span className="text-white mt-0.5 block">
                        Apache 2.0 (AndroidX Compose, Room, DataStore, Coil)
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Tab 5: Reset Options */}
              {settingsTab === 'RESET' && (
                <div className="rounded-[24px] p-6 bg-[#111827]/85 border border-white/15 space-y-4">
                  <h2 className="text-lg font-display font-semibold text-white">
                    Reset TV LAUNCHER Preferences
                  </h2>
                  <p className="text-sm text-[#AAB4C8]">
                    Restore appearance, default favourites dock, or perform a complete local factory reset.
                  </p>
                  <div className="flex flex-wrap gap-3">
                    <button
                      type="button"
                      onClick={handleResetAppearance}
                      className="px-4 py-2.5 rounded-xl text-xs font-medium bg-white/10 hover:bg-white/15 text-white"
                    >
                      Reset Appearance
                    </button>
                    <button
                      type="button"
                      onClick={handleResetFavourites}
                      className="px-4 py-2.5 rounded-xl text-xs font-medium bg-white/10 hover:bg-white/15 text-white"
                    >
                      Reset Favourites Dock
                    </button>
                    {!confirmFactoryReset ? (
                      <button
                        type="button"
                        onClick={() => setConfirmFactoryReset(true)}
                        className="px-4 py-2.5 rounded-xl text-xs font-semibold bg-red-500/20 border border-red-400/40 text-red-300 hover:bg-red-500/30"
                      >
                        Complete Local Settings Reset
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={handleFullFactoryReset}
                        className="px-4 py-2.5 rounded-xl text-xs font-bold bg-red-500 text-white"
                      >
                        Confirm Complete Reset Now
                      </button>
                    )}
                  </div>
                </div>
              )}
            </section>
          )}

          {/* SCREEN 7: NATIVE ANDROID STUDIO PROJECT & APK EXPORT */}
          {activeScreen === 'ANDROID_PROJECT' && (
            <AndroidStudioProjectView accentHex={accentHex} />
          )}
        </main>
      </div>

      {/* Launched Application Simulation Modal */}
      {launchedAppModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="launched-app-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-md"
        >
          <div className="max-w-lg w-full rounded-[28px] bg-[#111827] border border-white/25 p-7 shadow-2xl space-y-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-emerald-400">
                INTENT.ACTION_MAIN · CATEGORY_LEANBACK_LAUNCHER
              </span>
              <button
                type="button"
                onClick={() => setLaunchedAppModal(null)}
                aria-label="Return to TV LAUNCHER"
                className="p-1.5 rounded-lg text-[#AAB4C8] hover:text-white hover:bg-white/10"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="flex items-center gap-4">
              <div
                className="w-16 h-16 rounded-2xl flex items-center justify-center shrink-0 shadow-lg"
                style={{
                  background: `linear-gradient(135deg, ${launchedAppModal.brandColor} 0%, ${launchedAppModal.accentColor} 100%)`,
                }}
              >
                {renderAppIconGlyph(launchedAppModal.iconCode, 'w-8 h-8 text-white')}
              </div>
              <div>
                <h2
                  id="launched-app-title"
                  className="text-2xl font-display font-bold text-white"
                >
                  {launchedAppModal.appName}
                </h2>
                <p className="text-xs font-mono text-[#AAB4C8]">
                  {launchedAppModal.packageName}
                </p>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-[#080B14] border border-white/10 font-mono text-xs text-slate-300 space-y-1">
              <div>Activity: {launchedAppModal.activityName}</div>
              <div>Flags: FLAG_ACTIVITY_NEW_TASK</div>
              <div>Status: Activity started & added to Recently Used Apps</div>
            </div>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setLaunchedAppModal(null)}
                style={{ backgroundColor: accentHex }}
                className="px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-950"
              >
                Return to TV LAUNCHER Home
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Ambient Liquid Glass Clock Screensaver Overlay */}
      {screensaverActive && (
        <div
          onClick={() => setScreensaverActive(false)}
          role="button"
          tabIndex={0}
          aria-label="Exit Screensaver"
          className="fixed inset-0 z-50 bg-[#05070E] flex flex-col items-center justify-center cursor-pointer overflow-hidden"
        >
          <div
            className="w-96 h-96 rounded-full blur-3xl opacity-30 absolute -top-10 -left-10"
            style={{ background: accentHex }}
          />
          <div className="w-96 h-96 rounded-full blur-3xl opacity-25 bg-indigo-500 absolute bottom-0 right-0" />
          <div className="relative z-10 text-center space-y-3">
            <div className="text-6xl md:text-8xl font-display font-bold text-white tracking-tight tabular-nums">
              {formattedTime}
            </div>
            <div className="text-lg font-mono text-[#AAB4C8]">
              {formattedDate} · TV LAUNCHER BY SHANAWAZ
            </div>
            <div className="text-xs text-sky-300 pt-6">
              Press any remote key or click anywhere to wake television
            </div>
          </div>
        </div>
      )}

      {/* Interactive Android TV Remote D-Pad Controller */}
      <RemoteControlPanel
        isOpen={remoteOpen}
        accentHex={accentHex}
        onClose={() => setRemoteOpen(false)}
        onDpadDirection={handleDpadStep}
        onDpadSelect={handleDpadSelect}
        onDpadBack={() => setActiveScreen('HOME')}
        onDpadHome={() => setActiveScreen('HOME')}
        onQuickSearch={() => setActiveScreen('SEARCH')}
        onQuickSettings={() => setActiveScreen('SETTINGS')}
        onTriggerScreensaver={() => setScreensaverActive(true)}
      />
    </div>
  );
}
