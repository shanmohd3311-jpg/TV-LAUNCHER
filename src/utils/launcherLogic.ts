export type SortOrder = 'ALPHABETICAL_ASC' | 'ALPHABETICAL_DESC' | 'RECENTLY_USED' | 'CUSTOM_ORDER';
export type OperatingMode = 'DEFAULT_LAUNCHER' | 'NORMAL_APPLICATION';
export type DeviceFirmwareProfile =
  | 'STANDARD_ANDROID_TV'
  | 'GOOGLE_TV_UNLOCKED'
  | 'GOOGLE_TV_RESTRICTED_HOME';

export interface TVAppItem {
  packageName: string;
  activityName: string;
  appName: string;
  category: 'Streaming' | 'Media & Plex' | 'Gaming' | 'Utilities' | 'System';
  isLeanbackNative: boolean;
  isSystemApp: boolean;
  versionName: string;
  brandColor: string;
  accentColor: string;
  iconCode: string;
  description: string;
  isFavourite: boolean;
  favouriteOrder: number;
  customOrder: number;
  isHidden: boolean;
  lastLaunchedTimestamp: number;
}

export interface LauncherModeEvaluation {
  selectedMode: OperatingMode;
  effectiveMode: OperatingMode;
  isSystemDefaultHome: boolean;
  canOpenHomeSettings: boolean;
  isFirmwareRestricted: boolean;
  currentSystemHomePackage: string;
  statusBadge: string;
  compatibilityMessage: string;
  manualInstructions: string[];
}

/**
 * Filters, searches, and sorts installed TV applications while respecting hidden state.
 */
export function filterAndSortApps(
  apps: TVAppItem[],
  options: {
    searchQuery?: string;
    sortOrder?: SortOrder;
    includeHidden?: boolean;
    categoryFilter?: string;
  }
): TVAppItem[] {
  const {
    searchQuery = '',
    sortOrder = 'ALPHABETICAL_ASC',
    includeHidden = false,
    categoryFilter = 'ALL',
  } = options;

  const normalizedQuery = searchQuery.trim().toLowerCase();

  const filtered = apps.filter((app) => {
    if (!includeHidden && app.isHidden) return false;
    if (categoryFilter !== 'ALL' && app.category !== categoryFilter) return false;
    if (normalizedQuery.length > 0) {
      const matchesName = app.appName.toLowerCase().includes(normalizedQuery);
      const matchesPkg = app.packageName.toLowerCase().includes(normalizedQuery);
      const matchesCategory = app.category.toLowerCase().includes(normalizedQuery);
      return matchesName || matchesPkg || matchesCategory;
    }
    return true;
  });

  return [...filtered].sort((a, b) => {
    switch (sortOrder) {
      case 'ALPHABETICAL_ASC':
        return a.appName.localeCompare(b.appName);
      case 'ALPHABETICAL_DESC':
        return b.appName.localeCompare(a.appName);
      case 'RECENTLY_USED':
        if (b.lastLaunchedTimestamp !== a.lastLaunchedTimestamp) {
          return b.lastLaunchedTimestamp - a.lastLaunchedTimestamp;
        }
        return a.appName.localeCompare(b.appName);
      case 'CUSTOM_ORDER':
        return a.customOrder - b.customOrder;
      default:
        return a.appName.localeCompare(b.appName);
    }
  });
}

/**
 * Extracts and sorts the Liquid Glass Favourites Dock apps.
 */
export function getFavouriteDockApps(apps: TVAppItem[]): TVAppItem[] {
  return apps
    .filter((app) => app.isFavourite && !app.isHidden)
    .sort((a, b) => a.favouriteOrder - b.favouriteOrder);
}

/**
 * Extracts recently launched applications sorted newest first.
 */
export function getRecentlyUsedApps(apps: TVAppItem[], limit = 6): TVAppItem[] {
  return apps
    .filter((app) => !app.isHidden && app.lastLaunchedTimestamp > 0)
    .sort((a, b) => b.lastLaunchedTimestamp - a.lastLaunchedTimestamp)
    .slice(0, limit);
}

/**
 * Toggles an application's favourite status and assigns a clean contiguous favouriteOrder.
 */
export function toggleAppFavourite(apps: TVAppItem[], packageName: string): TVAppItem[] {
  const target = apps.find((a) => a.packageName === packageName);
  if (!target) return apps;

  const makingFavourite = !target.isFavourite;
  const currentMaxOrder = apps
    .filter((a) => a.isFavourite)
    .reduce((max, a) => Math.max(max, a.favouriteOrder), -1);

  return apps.map((app) => {
    if (app.packageName !== packageName) return app;
    return {
      ...app,
      isFavourite: makingFavourite,
      favouriteOrder: makingFavourite ? currentMaxOrder + 1 : 999,
    };
  });
}

/**
 * Reorders an app inside the Favourite Applications Dock left (-1) or right (+1).
 */
export function reorderFavouriteApps(
  apps: TVAppItem[],
  packageName: string,
  direction: -1 | 1
): TVAppItem[] {
  const dock = getFavouriteDockApps(apps);
  const currentIndex = dock.findIndex((a) => a.packageName === packageName);
  if (currentIndex === -1) return apps;

  const targetIndex = currentIndex + direction;
  if (targetIndex < 0 || targetIndex >= dock.length) return apps;

  const reorderedDock = [...dock];
  const [moved] = reorderedDock.splice(currentIndex, 1);
  reorderedDock.splice(targetIndex, 0, moved);

  const orderMap = new Map<string, number>();
  reorderedDock.forEach((item, idx) => orderMap.set(item.packageName, idx));

  return apps.map((app) => {
    if (orderMap.has(app.packageName)) {
      return { ...app, favouriteOrder: orderMap.get(app.packageName)! };
    }
    return app;
  });
}

/**
 * Records an app launch timestamp for Recently Used history.
 */
export function recordAppLaunch(
  apps: TVAppItem[],
  packageName: string,
  timestamp = Date.now()
): TVAppItem[] {
  return apps.map((app) =>
    app.packageName === packageName
      ? { ...app, lastLaunchedTimestamp: timestamp }
      : app
  );
}

/**
 * Evaluates Dual Mode (MODE 1: DEFAULT_LAUNCHER vs MODE 2: NORMAL_APPLICATION)
 * with honest compatibility checks and graceful fallback on restricted Google TV firmwares.
 */
export function evaluateLauncherModeStatus(
  selectedMode: OperatingMode,
  firmwareProfile: DeviceFirmwareProfile,
  userGrantedSystemDefaultHome: boolean
): LauncherModeEvaluation {
  const isFirmwareRestricted = firmwareProfile === 'GOOGLE_TV_RESTRICTED_HOME';
  const canOpenHomeSettings = !isFirmwareRestricted;

  const isSystemDefaultHome =
    !isFirmwareRestricted &&
    selectedMode === 'DEFAULT_LAUNCHER' &&
    userGrantedSystemDefaultHome;

  // Graceful fallback to NORMAL_APPLICATION if device restricts third-party Home or not yet granted
  const effectiveMode: OperatingMode = isSystemDefaultHome
    ? 'DEFAULT_LAUNCHER'
    : 'NORMAL_APPLICATION';

  const currentSystemHomePackage = isSystemDefaultHome
    ? 'com.shanawaz.tvlauncher'
    : firmwareProfile === 'STANDARD_ANDROID_TV'
      ? 'com.google.android.tvlauncher'
      : 'com.google.android.apps.tv.launcherx';

  let statusBadge = 'NORMAL APPLICATION (Active)';
  let compatibilityMessage =
    'Compatible Android TV device: Eligible for Default Home selection via System Settings, or run manually as a Normal Application.';

  if (isSystemDefaultHome) {
    statusBadge = 'DEFAULT LAUNCHER (Verified System Home)';
    compatibilityMessage =
      'TV LAUNCHER (com.shanawaz.tvlauncher) is currently registered and active as your television Home interface.';
  } else if (isFirmwareRestricted) {
    statusBadge =
      selectedMode === 'DEFAULT_LAUNCHER'
        ? 'NORMAL APPLICATION (Graceful Fallback — Firmware Restricted)'
        : 'NORMAL APPLICATION (Active)';
    compatibilityMessage =
      'This Google TV firmware locks the system Home role to LauncherX and disables third-party Home replacement. TV LAUNCHER automatically operates in Normal Application Mode with 100% dashboard functionality.';
  } else if (selectedMode === 'DEFAULT_LAUNCHER' && !userGrantedSystemDefaultHome) {
    statusBadge = 'PENDING SYSTEM HOME SELECTION (Running in Normal Mode)';
    compatibilityMessage =
      'Default Launcher mode selected. Click "SET AS DEFAULT LAUNCHER" to open the Android TV Home-app chooser and confirm TV LAUNCHER.';
  }

  const manualInstructions = [
    '1. Standard Android TV: Click "SET AS DEFAULT LAUNCHER" to open Settings -> Apps -> Default Apps -> Home App, then select TV LAUNCHER.',
    '2. Normal Application Mode: Open TV LAUNCHER anytime from your installed Android TV / Google TV app row without altering your current Home app.',
    '3. Restricted Google TV Firmware: If your television manufacturer disables third-party Home selection, use Normal Application Mode — no root, ADB hacks, or private APIs are ever used.',
  ];

  return {
    selectedMode,
    effectiveMode,
    isSystemDefaultHome,
    canOpenHomeSettings,
    isFirmwareRestricted,
    currentSystemHomePackage,
    statusBadge,
    compatibilityMessage,
    manualInstructions,
  };
}
