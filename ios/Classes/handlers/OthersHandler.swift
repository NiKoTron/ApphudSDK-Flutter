//
//  OthersHandler.swift
//  appHud
//
//  Created by Stanislav on 05.02.2021.
//

import Foundation

class OthersHandler: Handler {
    typealias AssociatedEnum = AppHudMethod.Others

    func tryToHandle(method: String, args: [String : Any]?, result: @escaping FlutterResult) {
        switch method {
        case AssociatedEnum.enableDebugLogs.rawValue:
            break
        case AssociatedEnum.isSandbox.rawValue:
            break
        default:
            break
        }
    }
}
