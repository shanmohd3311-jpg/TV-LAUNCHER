import test from 'node:test';
import assert from 'node:assert/strict';
import {
  filterAndSortApps,
  getFavouriteDockApps,
  getRecentlyUsedApps,
  toggleAppFavourite,
  reorderFavouriteApps,
  recordAppLaunch,
  evaluateLauncherModeStatus,
  type TVAppItem,
} from './launcherLogic';

const SAMPLE_APPS: TVAppItem[] = [
  {
    packageName: 'com.google.android.youtube.tv',
    activityName: 'com.google.android.apps.youtube.tv.activity.ShellActivity',
    appName: 'YouTube for TV',
    category: 'Streaming',
    isLeanbackNative: true,
    isSystemApp: false,
    versionName: '4.42.303',
    brandColor: '#EF4444',
    accentColor: '#F87171',
    iconCode: 'youtube',
    description: '4K HDR streaming & subscriptions',
    isFavourite: true,
    favouriteOrder: 0,
    customOrder: 0,
    isHidden: false,
    lastLaunchedTimestamp: 1000,
  },
  {
    packageName: 'com.netflix.ninja',
    activityName: 'com.netflix.ninja.MainActivity',
    appName: 'Netflix',
    category: 'Streaming',
    isLeanbackNative: true,
    isSystemApp: false,
    versionName: '11.2.0',
    brandColor: '#DC2626',
    accentColor: '#EF4444',
    iconCode: 'film',
    description: 'Dolby Vision cinema & series',
    isFavourite: true,
    favouriteOrder: 1,
    customOrder: 1,
    isHidden: false,
    lastLaunchedTimestamp: 2000,
  },
  {
    packageName: 'com.plexapp.android',
    activityName: 'com.plexapp.plex.activities.SplashActivity',
    appName: 'Plex Media Server',
    category: 'Media & Plex',
    isLeanbackNative: true,
    isSystemApp: false,
    versionName: '10.18.0',
    brandColor: '#D97706',
    accentColor: '#FBBF24',
    iconCode: 'server',
    description: 'Local NAS & home theater streaming',
    isFavourite: false,
    favouriteOrder: 999,
    customOrder: 2,
    isHidden: false,
    lastLaunchedTimestamp: 0,
  },
  {
    packageName: 'org.videolan.vlc',
    activityName: 'org.videolan.vlc.StartActivity',
    appName: 'VLC for Android TV',
    category: 'Media & Plex',
    isLeanbackNative: true,
    isSystemApp: false,
    versionName: '3.6.0',
    brandColor: '#EA580C',
    accentColor: '#FB923C',
    iconCode: 'play',
    description: 'Universal MKV & network stream player',
    isFavourite: false,
    favouriteOrder: 999,
    customOrder: 3,
    isHidden: true,
    lastLaunchedTimestamp: 500,
  },
];

test('filterAndSortApps excludes hidden apps by default and sorts alphabetically', () => {
  const result = filterAndSortApps(SAMPLE_APPS, { sortOrder: 'ALPHABETICAL_ASC' });
  assert.equal(result.length, 3);
  assert.deepEqual(
    result.map((a) => a.appName),
    ['Netflix', 'Plex Media Server', 'YouTube for TV']
  );
});

test('filterAndSortApps filters by search query across name and package', () => {
  const byName = filterAndSortApps(SAMPLE_APPS, { searchQuery: 'plex' });
  assert.equal(byName.length, 1);
  assert.equal(byName[0].packageName, 'com.plexapp.android');

  const byPkg = filterAndSortApps(SAMPLE_APPS, { searchQuery: 'ninja' });
  assert.equal(byPkg.length, 1);
  assert.equal(byPkg[0].appName, 'Netflix');
});

test('toggleAppFavourite and reorderFavouriteApps update dock order deterministically', () => {
  const withPlexPinned = toggleAppFavourite(SAMPLE_APPS, 'com.plexapp.android');
  const dock = getFavouriteDockApps(withPlexPinned);
  assert.equal(dock.length, 3);
  assert.equal(dock[2].packageName, 'com.plexapp.android');

  const movedLeft = reorderFavouriteApps(withPlexPinned, 'com.plexapp.android', -1);
  const reorderedDock = getFavouriteDockApps(movedLeft);
  assert.deepEqual(
    reorderedDock.map((a) => a.packageName),
    ['com.google.android.youtube.tv', 'com.plexapp.android', 'com.netflix.ninja']
  );
});

test('recordAppLaunch updates Recently Used apps order', () => {
  const updated = recordAppLaunch(SAMPLE_APPS, 'com.plexapp.android', 9500);
  const recents = getRecentlyUsedApps(updated);
  assert.equal(recents[0].packageName, 'com.plexapp.android');
  assert.equal(recents[1].packageName, 'com.netflix.ninja');
});

test('evaluateLauncherModeStatus handles Mode 1 Default Launcher, Mode 2 Normal App, and restricted firmware fallback without false success', () => {
  // 1. Standard Android TV where user sets TV LAUNCHER as default Home
  const activeDefault = evaluateLauncherModeStatus(
    'DEFAULT_LAUNCHER',
    'STANDARD_ANDROID_TV',
    true
  );
  assert.equal(activeDefault.isSystemDefaultHome, true);
  assert.equal(activeDefault.effectiveMode, 'DEFAULT_LAUNCHER');
  assert.equal(activeDefault.currentSystemHomePackage, 'com.shanawaz.tvlauncher');

  // 2. Restricted Google TV firmware where manufacturer blocks 3rd-party Home replacement
  const restrictedFallback = evaluateLauncherModeStatus(
    'DEFAULT_LAUNCHER',
    'GOOGLE_TV_RESTRICTED_HOME',
    true
  );
  assert.equal(restrictedFallback.isSystemDefaultHome, false);
  assert.equal(restrictedFallback.isFirmwareRestricted, true);
  assert.equal(restrictedFallback.effectiveMode, 'NORMAL_APPLICATION');

  // 3. Normal Application mode
  const normalMode = evaluateLauncherModeStatus(
    'NORMAL_APPLICATION',
    'GOOGLE_TV_UNLOCKED',
    false
  );
  assert.equal(normalMode.effectiveMode, 'NORMAL_APPLICATION');
  assert.equal(normalMode.isSystemDefaultHome, false);
});
