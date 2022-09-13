package com.zaihui.installplugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
        context = flutterPluginBinding.applicationContext;
        contextReference = WeakReference(flutterPluginBinding.applicationContext)

    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        val method = call.method
        if (method == "installApp") {
            val filePath = call.argument<String>("filePath")
            if (filePath == null || filePath.isEmpty()) {
                result.error("-1", "FilePath Must Not Null", null)
                return
            }
            installApk(filePath, result)
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

    private fun installApk(filePath: String, result: MethodChannel.Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (hasInstallPermission()) {
                val intent = AppUtils.getInstallAppIntent(context, filePath, false)
                if (intent == null) {
                    result.error("-2", "Not Get Install Intent", null)
                    return
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                activity?.startActivityForResult(intent, 1024)
                result.success(0)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:" + activity?.packageName)
                activity?.startActivityForResult(intent, 1023)
                apkFilePath = filePath
                result.notImplemented()
                channel.invokeMethod("onPermissionRequest", null)

            }
        } else {
            val intent = AppUtils.getInstallAppIntent(context, filePath, false)
            if (intent == null) {
                result.error("-2", "Not Get Install Intent", null)
                return
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            activity?.startActivityForResult(intent, 1024)
            result.success(0)
        }
    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == 1024) {
            onInstallApkCallback(resultCode, data)
            return true
        }
        if (requestCode == 1023) {
            if (resultCode == Activity.RESULT_OK && apkFilePath.isNotEmpty()) {
                val intent = AppUtils.getInstallAppIntent(context, apkFilePath, false)
                if (intent == null) {
                    channel.invokeMethod("onError", -2)
                    return true
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                activity?.startActivityForResult(intent, 1024)
            }
            return true
        }
        return false

    }

    private fun onInstallApkCallback(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            channel.invokeMethod("onInstallResult", true)
        } else {
            channel.invokeMethod("onInstallResult", false)
        }
    }

    private fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager?.canRequestPackageInstalls() ?: false
        } else {
            return true
        }
    }

}