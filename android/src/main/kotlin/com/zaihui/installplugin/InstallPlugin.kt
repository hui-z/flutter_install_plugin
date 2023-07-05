package com.zaihui.installplugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.lang.ref.WeakReference

class InstallPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var mResult: MethodChannel.Result? = null
    private var context: Context? = null
    private val activity get() = activityReference.get()
    private var activityReference = WeakReference<Activity>(null)

    private val REQUEST_CODE_PERMISSION_OR_INSTALL = 1024

    private var apkFilePath = ""
    private var hasPermission = false

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "install_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        context = null
        channel.setMethodCallHandler(null)
        mResult = null
    }

    // ActivityAware
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityReference = WeakReference(binding.activity)
        binding.addActivityResultListener { requestCode, resultCode, data ->
            return@addActivityResultListener handleActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityReference.clear()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activityReference = WeakReference(binding.activity)
        binding.addActivityResultListener { requestCode, resultCode, data ->
            return@addActivityResultListener handleActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDetachedFromActivity() {
        activityReference.clear()
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        mResult = result
        when (call.method) {
            "installApk" -> {
                val filePath = call.argument<String>("filePath")
                val packageName = call.argument<String>("packageName")
                Log.i("test","onMethodCall:$filePath,$packageName")
                installApk(filePath,packageName)
            }
            else -> result.notImplemented()
        }
    }

    private fun installApk(filePath: String?,packageName:String?) {
        if (filePath.isNullOrEmpty()) {
            mResult?.success(SaveResultModel(false, "FilePath Must Not Null").toHashMap())
            return
        }
        apkFilePath = filePath
        val pName = if (packageName.isNullOrEmpty()) {
            context?.packageName
        } else {
            packageName
        }
        if (pName.isNullOrEmpty()) {
            mResult?.success(
                SaveResultModel(
                    false,
                    "Failed To Obtain PackageName Must Not Null"
                ).toHashMap()
            )
            return
        }
        if (hasInstallPermission()) {
            hasPermission = true
            // begin install
            val intent = getInstallAppIntent(context, pName, filePath, false)
            if (intent == null) {
                mResult?.success(SaveResultModel(false, "Not Get Install Intent").toHashMap())
                return
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            activity?.startActivityForResult(intent, REQUEST_CODE_PERMISSION_OR_INSTALL)
        } else {
            hasPermission = false
            // request install permission
            requestInstallPermission(pName)
        }
    }

    private fun getInstallAppIntent(context: Context?,packageName:String, filePath: String?, newTask: Boolean): Intent? {
        if (context == null) return null
        if (filePath.isNullOrEmpty()) return null
        var file = File(filePath)
        if (!file.exists()) return null
        Log.i("test","getInstallAppIntent:${Build.VERSION.SDK_INT}")
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
            val storePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            // DIRECTORY_DOWNLOADS
            val downloadsDir = File(storePath).apply {
                if (!exists()) {
                    mkdir()
                }
            }
            // DIRECTORY_DOWNLOADS / packageName
            val downloadsAppDir = File(downloadsDir,packageName).apply {
                if (!exists()) {
                    mkdir()
                }
            }
            Log.i("test","getInstallAppIntent:$storePath")
            val destFile = File(downloadsAppDir, file.name)
            file.copyTo(destFile, overwrite = true)
            file = destFile
        }

        val uri: Uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(file)
        } else {
            InstallFileProvider.getUriForFile(context, file)
        }
        Log.i("test","getInstallAppIntent:$uri")
        val intent = Intent(Intent.ACTION_VIEW)
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(uri, type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        return if (!newTask) {
            intent
        } else intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.i("InstallPlugin", "handleActivityResult($requestCode,$resultCode,$data)")
        if (requestCode == REQUEST_CODE_PERMISSION_OR_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                if (hasPermission) {
                    mResult?.success(SaveResultModel(true, "Install Success").toHashMap())
                } else {
                    installApk(apkFilePath,"")
                }
            } else {
                if (hasPermission) {
                    mResult?.success(SaveResultModel(false, "Install Cancel").toHashMap())
                } else {
                    mResult?.success(
                        SaveResultModel(
                            false,
                            "Request Install Permission Fail"
                        ).toHashMap()
                    )
                }
            }
            return true
        }
        return false
    }

    private fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.packageManager?.canRequestPackageInstalls() ?: false
        } else {
            return true
        }
    }

    private fun requestInstallPermission(packageName: String) {
        // request install permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            activity?.startActivityForResult(intent, REQUEST_CODE_PERMISSION_OR_INSTALL)
        }
    }
}

class SaveResultModel(
    private var isSuccess: Boolean,
    private var errorMessage: String? = null) {
    fun toHashMap(): HashMap<String, Any?> {
        val hashMap = HashMap<String, Any?>()
        hashMap["isSuccess"] = isSuccess
        hashMap["errorMessage"] = errorMessage
        return hashMap
    }
}

class InstallFileProvider : FileProvider() {
    companion object {
        fun getUriForFile(context: Context, file: File): Uri {
            val authority = "${context.packageName}.installFileProvider.install"
            return getUriForFile(context, authority, file)
        }
    }
}