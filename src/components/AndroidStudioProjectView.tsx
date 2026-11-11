import React, { useEffect, useState } from 'react';
import {
  Download,
  FileCode2,
  FolderGit2,
  Copy,
  Check,
  Terminal,
  ShieldAlert,
} from 'lucide-react';

interface ProjectFileEntry {
  path: string;
  name: string;
  category: string;
  lines: number;
  sizeBytes: number;
  content: string;
}

interface AndroidProjectResponse {
  projectName: string;
  packageName: string;
  developer: string;
  versionName: string;
  versionCode: number;
  minSdk: number;
  targetSdk: number;
  totalFiles: number;
  totalLines: number;
  environmentReport: {
    containerRuntime: string;
    androidSdkInstalled: boolean;
    gradleBinaryInstalled: boolean;
    apkBuildNote: string;
  };
  files: ProjectFileEntry[];
}

export const AndroidStudioProjectView: React.FC<{ accentHex: string }> = ({
  accentHex,
}) => {
  const [projectData, setProjectData] = useState<AndroidProjectResponse | null>(null);
  const [selectedPath, setSelectedPath] = useState<string>(
    'app/src/main/AndroidManifest.xml'
  );
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/android-project')
      .then((r) => r.json())
      .then((data: AndroidProjectResponse) => {
        setProjectData(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const selectedFile =
    projectData?.files.find((f) => f.path === selectedPath) ||
    projectData?.files[0] ||
    null;

  const handleCopyCode = () => {
    if (!selectedFile) return;
    navigator.clipboard?.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner & Download Action */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 rounded-[24px] p-6 bg-[#111827]/90 border border-white/15">
        <div className="space-y-1">
          <div className="text-xs font-mono text-sky-400">
            COMPLETE NATIVE ANDROID STUDIO PROJECT · KOTLIN + JETPACK COMPOSE FOR TV
          </div>
          <h1 className="text-2xl font-display font-bold text-white">
            TV LAUNCHER Native Source & APK Build Package
          </h1>
          <p className="text-sm text-[#AAB4C8] max-w-3xl">
            Package <span className="font-mono text-white">com.shanawaz.tvlauncher</span> · Developer: <span className="text-white font-medium">Shanawaz</span> · v1.0.0 (Code 1) · Min SDK 23 (Android 6.0) to Target SDK 35 · {projectData?.totalFiles ?? 28} native files ({projectData?.totalLines ?? 1500}+ lines of Kotlin, Room, DataStore & Gradle).
          </p>
        </div>

        <a
          href="/api/android-project/download"
          download="TV_LAUNCHER_com.shanawaz.tvlauncher_AndroidStudio.tar.gz"
          style={{ backgroundColor: accentHex }}
          className="inline-flex items-center gap-2.5 px-5 py-3 rounded-xl text-sm font-semibold text-slate-950 hover:opacity-95 transition-opacity whitespace-nowrap shrink-0"
        >
          <Download className="w-4 h-4" />
          <span>Download Android Studio Project (.tar.gz)</span>
        </a>
      </div>

      {/* Honest Environment & APK Build Instructions Banner */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="rounded-[20px] p-5 bg-[#111827]/75 border border-white/15 space-y-2">
          <div className="flex items-center gap-2 text-sm font-display font-semibold text-white">
            <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0" />
            <span>Environment Verification & Honest Build Status</span>
          </div>
          <p className="text-xs text-[#AAB4C8] leading-relaxed">
            {projectData?.environmentReport.apkBuildNote ||
              'This cloud preview container runs Linux + Node.js without the Android SDK or Gradle binaries installed. Therefore, no fake APK compilation is claimed. Download the complete native Android Studio project above to compile the signed Debug/Release APK or AAB.'}
          </p>
        </div>

        <div className="rounded-[20px] p-5 bg-[#111827]/75 border border-white/15 space-y-2">
          <div className="flex items-center gap-2 text-sm font-display font-semibold text-white">
            <Terminal className="w-4 h-4 text-sky-400 shrink-0" />
            <span>Android Studio & ADB TV Installation Commands</span>
          </div>
          <pre className="text-[11px] font-mono text-sky-200 bg-[#080B14] p-3 rounded-xl overflow-x-auto border border-white/10">
{`# 1. Extract & build Debug/Release APK or App Bundle:
tar -xzf TV_LAUNCHER_com.shanawaz.tvlauncher_AndroidStudio.tar.gz
./gradlew assembleDebug assembleRelease bundleRelease

# 2. Install onto physical Android TV / Google TV over Wi-Fi ADB:
adb connect <TV_IP_ADDRESS>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk`}
          </pre>
        </div>
      </div>

      {/* Interactive Native Kotlin & Manifest Source Explorer */}
      {loading ? (
        <div className="rounded-[24px] p-12 bg-[#111827]/70 border border-white/15 text-center text-sm text-[#AAB4C8]">
          Loading native Kotlin Jetpack Compose for TV project files...
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {/* File Tree Sidebar */}
          <div className="lg:col-span-4 rounded-[22px] bg-[#111827]/85 border border-white/15 p-4 max-h-[560px] overflow-y-auto space-y-1.5">
            <div className="px-2 pb-2 mb-2 border-b border-white/10 flex items-center justify-between text-xs text-[#AAB4C8]">
              <span className="inline-flex items-center gap-1.5 font-medium text-white">
                <FolderGit2 className="w-4 h-4 text-sky-400" />
                <span>Native Project Tree</span>
              </span>
              <span className="font-mono">{projectData?.files.length} files</span>
            </div>

            {projectData?.files.map((file) => {
              const active = file.path === selectedFile?.path;
              return (
                <button
                  key={file.path}
                  type="button"
                  onClick={() => setSelectedPath(file.path)}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs transition-colors flex items-center justify-between gap-2 ${
                    active
                      ? 'bg-sky-500/20 text-white border border-sky-400/40'
                      : 'text-[#AAB4C8] hover:text-white hover:bg-white/5'
                  }`}
                >
                  <span className="inline-flex items-center gap-2 truncate font-mono">
                    <FileCode2 className="w-3.5 h-3.5 shrink-0 text-sky-400" />
                    <span className="truncate">{file.path}</span>
                  </span>
                  <span className="text-[10px] font-mono text-[#AAB4C8] shrink-0">
                    {file.lines}L
                  </span>
                </button>
              );
            })}
          </div>

          {/* Code Viewer */}
          <div className="lg:col-span-8 rounded-[22px] bg-[#080B14]/95 border border-white/15 flex flex-col max-h-[560px]">
            <div className="px-5 py-3.5 border-b border-white/10 flex items-center justify-between gap-4">
              <div>
                <div className="font-mono text-xs text-white font-semibold">
                  {selectedFile?.path}
                </div>
                <div className="text-[11px] text-[#AAB4C8]">
                  {selectedFile?.lines} lines · {selectedFile?.sizeBytes} bytes
                </div>
              </div>

              <button
                type="button"
                onClick={handleCopyCode}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-white bg-white/10 hover:bg-white/15 transition-colors whitespace-nowrap shrink-0"
              >
                {copied ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Copied</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    <span>Copy File</span>
                  </>
                )}
              </button>
            </div>

            <pre className="p-5 text-xs font-mono text-slate-200 overflow-auto flex-1 leading-relaxed">
              <code>{selectedFile?.content}</code>
            </pre>
          </div>
        </div>
      )}
    </div>
  );
};
