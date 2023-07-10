import Flutter
import UIKit
    
public class SwiftInstallPlugin: NSObject, FlutterPlugin {

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "install_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftInstallPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "gotoAppStore":
        print(call.arguments ?? "null")
        guard let urlString = (call.arguments as? Dictionary<String, Any>)?["urlString"] as? String else {
            saveResult(result:result,isSuccess:false,errorMsg:"The parameter url cannot be empty")
            return
        }
        gotoAppStore(urlString: urlString,result:result)
    default:
        result(FlutterMethodNotImplemented)
    }
  }

    // Jump to the app's AppStore page
    func gotoAppStore(urlString: String, result: @escaping FlutterResult) {
        if let url = URL(string: urlString) {
            if UIApplication.shared.canOpenURL(url) {
               // According to the iOS system version, handle them separately
               if #available(iOS 10, *) {
                    UIApplication.shared.open(url, options: [:],completionHandler: {(success) in })
               } else {
                    UIApplication.shared.openURL(url)
               }
               saveResult(result:result,isSuccess:true,errorMsg:nil)
            }else{
               saveResult(result:result,isSuccess:false,errorMsg:"This url cannot jump to appstore, please check")
            }
        }
    }

    func saveResult(result: @escaping FlutterResult,isSuccess: Bool, errorMsg: String? = nil) {
        var saveResult = SaveResultModel()
        saveResult.isSuccess = isSuccess
        saveResult.errorMessage = errorMsg
        result(saveResult.toDic())
    }
}

public struct SaveResultModel: Encodable {
    var isSuccess: Bool!
    var errorMessage: String?

    func toDic() -> [String:Any]? {
        let encoder = JSONEncoder()
        guard let data = try? encoder.encode(self) else { return nil }
        if (!JSONSerialization.isValidJSONObject(data)) {
            return try? JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String:Any]
        }
        return nil
    }
}