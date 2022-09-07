package com.zaihui.installplugin;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public final class AppUtils {

    public static void installApp(Context context, final String filePath) {
        installApp(context, new File(filePath));
    }

    public static void installApp(Context context, final File file) {
        Intent installAppIntent = getInstallAppIntent(context, file,true);
        if (installAppIntent == null) return;
        context.startActivity(installAppIntent);
    }

    public static void installApp(Context context, final Uri uri) {
        Intent installAppIntent = getInstallAppIntent(uri,true);
        if (installAppIntent == null) {
            return;
        }
        context.startActivity(installAppIntent);
    }

    public static void uninstallApp(Context context, final String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        context.startActivity(getUninstallAppIntent(packageName));
    }

    public static boolean appIsInstalled(Context context, final String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(pkgName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void launchApp(Context context, final String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        Intent launchAppIntent = getLaunchAppIntent(context, packageName);
        if (launchAppIntent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.");
            return;
        }
        context.startActivity(launchAppIntent);
    }

    public static void relaunchApp(Context context, final boolean isKillProcess) {
        Intent intent = getLaunchAppIntent(context, context.getPackageName());
        if (intent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.");
            return;
        }
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        context.startActivity(intent);
        if (!isKillProcess) return;
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static void launchAppDetailsSettings(Context context, final String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) return;
        Intent intent = getLaunchAppDetailsSettingsIntent(pkgName, true);
        if (!isIntentAvailable(context, intent)) return;
        context.startActivity(intent);
    }

    public static void exitApp() {
        System.exit(0);
    }

    public static boolean isFirstInstall(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(getAppPackageName(context), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(getAppPackageName(context), 0).lastUpdateTime;
            return firstInstallTime == lastUpdateTime;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAppUpgraded(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(getAppPackageName(context), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(getAppPackageName(context), 0).lastUpdateTime;
            return firstInstallTime != lastUpdateTime;
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    private static String getAppPackageName(Context context) {
        return context.getPackageName();
    }

    @Nullable
    public static AppUtils.AppInfo getApkInfo(Context context, final File apkFile) {
        if (apkFile == null || !apkFile.isFile() || !apkFile.exists()) return null;
        return getApkInfo(context, apkFile.getAbsolutePath());
    }

    @Nullable
    public static AppUtils.AppInfo getApkInfo(Context context, final String apkFilePath) {
        if (apkFilePath == null || apkFilePath.isEmpty()) return null;
        PackageManager pm = context.getPackageManager();
        if (pm == null) return null;
        PackageInfo pi = pm.getPackageArchiveInfo(apkFilePath, 0);
        if (pi == null) return null;
        ApplicationInfo appInfo = pi.applicationInfo;
        appInfo.sourceDir = apkFilePath;
        appInfo.publicSourceDir = apkFilePath;
        return getBean(pm, pi);
    }

    private static AppInfo getBean(final PackageManager pm, final PackageInfo pi) {
        if (pi == null) return null;
        String versionName = pi.versionName;
        int versionCode = pi.versionCode;
        String packageName = pi.packageName;
        ApplicationInfo ai = pi.applicationInfo;
        if (ai == null) {
            return new AppInfo(packageName, "", null, "", versionName, versionCode, -1, -1, false);
        }
        String name = ai.loadLabel(pm).toString();
        Drawable icon = ai.loadIcon(pm);
        String packagePath = ai.sourceDir;
        int minSdkVersion = -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            minSdkVersion = ai.minSdkVersion;
        }
        int targetSdkVersion = ai.targetSdkVersion;
        boolean isSystem = (ApplicationInfo.FLAG_SYSTEM & ai.flags) != 0;
        return new AppInfo(packageName, name, icon, packagePath, versionName, versionCode, minSdkVersion, targetSdkVersion, isSystem);
    }

    /**
     * The application's information.
     */
    public static class AppInfo {

        private String packageName;
        private String name;
        private Drawable icon;
        private String packagePath;
        private String versionName;
        private int versionCode;
        private int minSdkVersion;
        private int targetSdkVersion;
        private boolean isSystem;

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(final Drawable icon) {
            this.icon = icon;
        }

        public boolean isSystem() {
            return isSystem;
        }

        public void setSystem(final boolean isSystem) {
            this.isSystem = isSystem;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(final String packageName) {
            this.packageName = packageName;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getPackagePath() {
            return packagePath;
        }

        public void setPackagePath(final String packagePath) {
            this.packagePath = packagePath;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(final int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(final String versionName) {
            this.versionName = versionName;
        }

        public int getMinSdkVersion() {
            return minSdkVersion;
        }

        public void setMinSdkVersion(int minSdkVersion) {
            this.minSdkVersion = minSdkVersion;
        }

        public int getTargetSdkVersion() {
            return targetSdkVersion;
        }

        public void setTargetSdkVersion(int targetSdkVersion) {
            this.targetSdkVersion = targetSdkVersion;
        }

        public AppInfo(String packageName, String name, Drawable icon, String packagePath, String versionName, int versionCode, int minSdkVersion, int targetSdkVersion, boolean isSystem) {
            this.setName(name);
            this.setIcon(icon);
            this.setPackageName(packageName);
            this.setPackagePath(packagePath);
            this.setVersionName(versionName);
            this.setVersionCode(versionCode);
            this.setMinSdkVersion(minSdkVersion);
            this.setTargetSdkVersion(targetSdkVersion);
            this.setSystem(isSystem);
        }
    }

    private static boolean isFileExists(Context context, final File file) {
        if (file == null) return false;
        if (file.exists()) {
            return true;
        }
        return isFileExists(context, file.getAbsolutePath());
    }

    private static boolean isFileExists(Context context, final String filePath) {
        File file = getFileByPath(filePath);
        if (file == null) {
            return false;
        }

        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(context, filePath);
    }

    private static boolean isFileExistsApi29(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                ContentResolver cr = context.getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) return false;
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static File getFileByPath(final String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return new File(filePath);
    }

    public static Intent getInstallAppIntent(final Uri uri, boolean newTask) {
        if (uri == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (!newTask) {
            return intent;
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static Intent getInstallAppIntent(Context context, final String filePath,boolean newTask) {
        return getInstallAppIntent(context, getFileByPath(filePath),newTask);
    }

    public static Intent getInstallAppIntent(Context context, final File file,boolean newTask) {
        if (!isFileExists(context, file)) return null;
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            String authority = context.getPackageName() + ".installFileProvider.install";
            uri = InstallFileProvider.getUriForFile(context, authority, file);
        }
        return getInstallAppIntent(uri,newTask);
    }

    private static Intent getUninstallAppIntent(final String pkgName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + pkgName));
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private static Intent getLaunchAppIntent(Context context, final String pkgName) {
        String launcherActivity = getLauncherActivity(context, pkgName);
        if (launcherActivity == null || launcherActivity.isEmpty()) return null;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName(pkgName, launcherActivity);
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private static String getLauncherActivity(Context context, final String pkg) {
        if (pkg == null || pkg.isEmpty()) return "";
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(pkg);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.size() == 0) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    private static Intent getLaunchAppDetailsSettingsIntent(final String pkgName) {
        return getLaunchAppDetailsSettingsIntent(pkgName, false);
    }

    private static Intent getLaunchAppDetailsSettingsIntent(final String pkgName, final boolean isNewTask) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + pkgName));
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    private static boolean isIntentAvailable(Context context, final Intent intent) {
        return context
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }
}
