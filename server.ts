import express from 'express';
import fs from 'fs';
import path from 'path';
import { execFile } from 'child_process';
import { createServer as createViteServer } from 'vite';

const PORT = 3000;
const ROOT_DIR = process.cwd();

interface AndroidProjectFile {
  path: string;
  name: string;
  category: 'manifest' | 'gradle' | 'kotlin-ui' | 'kotlin-data' | 'kotlin-core' | 'resource';
  lines: number;
  sizeBytes: number;
  content: string;
}

const ANDROID_FILE_PATHS: Array<{ relPath: string; category: AndroidProjectFile['category'] }> = [
  { relPath: 'settings.gradle.kts', category: 'gradle' },
  { relPath: 'build.gradle.kts', category: 'gradle' },
  { relPath: 'gradle.properties', category: 'gradle' },
  { relPath: 'gradle/libs.versions.toml', category: 'gradle' },
  { relPath: 'app/build.gradle.kts', category: 'gradle' },
  { relPath: 'app/proguard-rules.pro', category: 'gradle' },
  { relPath: 'app/src/main/AndroidManifest.xml', category: 'manifest' },
  { relPath: 'app/src/main/res/values/strings.xml', category: 'resource' },
  { relPath: 'app/src/main/res/values/themes.xml', category: 'resource' },
  { relPath: 'app/src/main/res/drawable/tv_banner.xml', category: 'resource' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/MainActivity.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/navigation/LauncherNavigation.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/theme/Color.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/theme/Typography.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/theme/Theme.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/components/GlassCard.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/components/GlassDock.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/components/AppIcon.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/components/TopBar.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/components/FocusableAppCard.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/HomeScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/LauncherModeScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/AppLibraryScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/SearchScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/WallpaperScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/SettingsScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/ui/AboutScreen.kt', category: 'kotlin-ui' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/data/TVApp.kt', category: 'kotlin-data' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/data/AppRepository.kt', category: 'kotlin-data' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/data/SettingsRepository.kt', category: 'kotlin-data' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/database/FavouriteAppDao.kt', category: 'kotlin-data' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/database/LauncherDatabase.kt', category: 'kotlin-data' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/manager/AppDiscoveryManager.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/manager/AppLaunchManager.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/manager/WallpaperManager.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/viewmodel/HomeViewModel.kt', category: 'kotlin-core' },
  { relPath: 'app/src/main/java/com/shanawaz/tvlauncher/viewmodel/SettingsViewModel.kt', category: 'kotlin-core' },
];

function loadAndroidProjectFiles(): AndroidProjectFile[] {
  const results: AndroidProjectFile[] = [];
  for (const item of ANDROID_FILE_PATHS) {
    const fullPath = path.join(ROOT_DIR, item.relPath);
    if (fs.existsSync(fullPath)) {
      const content = fs.readFileSync(fullPath, 'utf-8');
      const stats = fs.statSync(fullPath);
      results.push({
        path: item.relPath,
        name: path.basename(item.relPath),
        category: item.category,
        lines: content.split('\n').length,
        sizeBytes: stats.size,
        content,
      });
    }
  }
  return results;
}

async function startServer() {
  const app = express();
  app.use(express.json());

  // API endpoint returning the complete native Android Studio project files from disk
  app.get('/api/android-project', (_req, res) => {
    try {
      const files = loadAndroidProjectFiles();
      const totalLines = files.reduce((acc, f) => acc + f.lines, 0);
      res.json({
        projectName: 'TV LAUNCHER',
        packageName: 'com.shanawaz.tvlauncher',
        developer: 'Shanawaz',
        versionName: '1.0.0',
        versionCode: 1,
        minSdk: 23,
        targetSdk: 35,
        totalFiles: files.length,
        totalLines,
        environmentReport: {
          containerRuntime: 'Linux Cloud Container (Node.js)',
          androidSdkInstalled: false,
          gradleBinaryInstalled: false,
          apkBuildNote:
            'This cloud container does not have Android SDK or Java/Gradle installed. All native Kotlin Jetpack Compose for TV source files, Room DAO, DataStore, Dual-Mode AndroidManifest.xml, and Gradle configurations are generated on disk and ready for download to compile in Android Studio.',
        },
        files,
      });
    } catch (error) {
      res.status(500).json({ error: 'Failed to read native Android project files.' });
    }
  });

  // API endpoint streaming a downloadable .tar.gz archive of the complete Android Studio project
  app.get('/api/android-project/download', (_req, res) => {
    const existingPaths = ANDROID_FILE_PATHS.map((f) => f.relPath).filter((rel) =>
      fs.existsSync(path.join(ROOT_DIR, rel))
    );

    const archivePath = path.join('/tmp', 'TV_LAUNCHER_Android_Studio_Project.tar.gz');
    execFile(
      'tar',
      ['-czf', archivePath, ...existingPaths],
      { cwd: ROOT_DIR },
      (err) => {
        if (err || !fs.existsSync(archivePath)) {
          res.status(500).json({ error: 'Could not create Android Studio project archive.' });
          return;
        }
        res.setHeader('Content-Type', 'application/gzip');
        res.setHeader(
          'Content-Disposition',
          'attachment; filename="TV_LAUNCHER_com.shanawaz.tvlauncher_AndroidStudio.tar.gz"'
        );
        const stream = fs.createReadStream(archivePath);
        stream.pipe(res);
      }
    );
  });

  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(ROOT_DIR, 'dist');
    app.use(express.static(distPath));
    app.get('*', (_req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`TV LAUNCHER server running at http://0.0.0.0:${PORT}`);
  });
}

startServer();
