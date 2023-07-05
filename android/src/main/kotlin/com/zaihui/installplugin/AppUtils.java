package com.zaihui.installplugin;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    public static void launchAppDetailsSettings(Context context, final String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) return;
        Intent intent = getLaunchAppDetailsSettingsIntent(pkgName, true);
        if (!isIntentAvailable(context, intent)) return;
        context.startActivity(intent);
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
