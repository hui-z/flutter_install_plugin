package com.zaihui.installplugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.lang.ref.WeakReference

class InstallPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private val activity get() = activityReference.get()
    private var activityReference = WeakReference<Activity>(null)
    private var contextReference = WeakReference<Context>(null)

    companion object {
        const val TAG = "InstallPlugin"
        const val ERROR_ARGUMENTS = "-1"
        const val ERROR_NOT_GET_INTENT = "-2"

        const val STATUS_REQUESTING_PERMISSION = "1"
        const val STATUS_INSTALLING = "2"
        const val STATUS_INSTALL_SUCCESS = "3"
        const val STATUS_INSTALL_FAIL = "4"

        const val REQUEST_CODE_INSTALL = 1024
        const val REQUEST_CODE_PERMISSION = 1024

        var apkFilePath = ""

        /// 保留旧版本的兼容
        fun registerWith(registerWith: Registrar) {
            val plugin = InstallPlugin()
            plugin.channel = MethodChannel(registerWith.messenger(), "install_plugin")
            plugin.channel.setMethodCallHandler(plugin)
            plugin.context = registerWith.context().applicationContext
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "install_plugin")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        contextReference = WeakReference(flutterPluginBinding.applicationContext)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        val method = call.method
        if (method == "installApk") {
            val filePath = call.argument<String>("filePath")
            val packageName = call.argument<String>("appId")
            if (filePath == null || filePath.isEmpty()) {
                result.error(ERROR_ARGUMENTS, "FilePath Must Not Null", null)
                return
            }
            if (packageName == null || packageName.isEmpty()) {
                result.error(ERROR_ARGUMENTS, "PackageName Must Not Null", null)
                return
            }
            installApk(filePath, packageName, result)
            return
        }
        result.notImplemented()
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

    private fun installApk(filePath: String, packageName: String, result: MethodChannel.Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (hasInstallPermission()) {
                val intent = AppUtils.getInstallAppIntent(context, filePath, false)
                if (intent == null) {
                    result.error(ERROR_NOT_GET_INTENT, "Not Get Install Intent", null)
                    return
                }
                result.success(0)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                activity?.startActivityForResult(intent, REQUEST_CODE_INSTALL)
                notifyFlutterClient(STATUS_INSTALLING)
            } else {
                result.success(0)
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:$packageName")
                activity?.startActivityForResult(intent, REQUEST_CODE_PERMISSION)
                apkFilePath = filePath
                notifyFlutterClient(STATUS_REQUESTING_PERMISSION)
            }
        } else {
            val intent = AppUtils.getInstallAppIntent(context, filePath, false)
            if (intent == null) {
                result.error(ERROR_NOT_GET_INTENT, "Not Get Install Intent", null)
                return
            }
            result.success(0)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            activity?.startActivityForResult(intent, 1024)
            notifyFlutterClient(STATUS_INSTALLING)
        }
    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.e(TAG, "handleActivityResult($requestCode,$resultCode,$data)")
        if (requestCode == REQUEST_CODE_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                notifyFlutterClient(STATUS_INSTALL_SUCCESS)
            } else {
                notifyFlutterClient(STATUS_INSTALL_FAIL)
            }
            return true
        }

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (resultCode == Activity.RESULT_OK && apkFilePath.isNotEmpty()) {
                val intent = AppUtils.getInstallAppIntent(context, apkFilePath, false)
                if (intent == null) {
                    notifyFlutterClient(STATUS_INSTALL_FAIL)
                    return true
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                activity?.startActivityForResult(intent, REQUEST_CODE_INSTALL)
                notifyFlutterClient(STATUS_INSTALLING)
            }
            return true
        }
        return false
    }

    private fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager?.canRequestPackageInstalls() ?: false
        } else {
            return true
        }
    }

    private fun notifyFlutterClient(code: String) {
        channel.invokeMethod("onInstallStatusChange", code)
    }
}