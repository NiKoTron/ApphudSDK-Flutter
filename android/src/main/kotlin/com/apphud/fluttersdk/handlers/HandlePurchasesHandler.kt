package com.apphud.fluttersdk.handlers

import android.content.Context
import com.apphud.sdk.Apphud
import com.google.gson.Gson
import io.flutter.plugin.common.MethodChannel
import com.apphud.sdk.domain.ApphudSubscription
import android.util.Log
import java.lang.IllegalStateException

class HandlePurchasesHandler(override val routes: List<String>, val context: Context) : Handler {

    private val gson: Gson by lazy { Gson() }

    override fun tryToHandle(method: String, args: Map<String, Any>?, result: MethodChannel.Result) {
        when (method) {
            HandlePurchasesRoutes.hasActiveSubscription.name -> hasActiveSubscription(result)
            HandlePurchasesRoutes.subscription.name -> subscription(result)
            HandlePurchasesRoutes.subscriptions.name -> subscriptions(result)
            HandlePurchasesRoutes.nonRenewingPurchases.name -> nonRenewingPurchases(result)
            HandlePurchasesRoutes.isNonRenewingPurchaseActive.name -> IsNonRenewingPurchaseActiveParser(result).parse(args) { productId ->
                isNonRenewingPurchaseActive(productId, result)
            }
            HandlePurchasesRoutes.restorePurchases.name -> restorePurchases(result)
            HandlePurchasesRoutes.migratePurchasesIfNeeded.name -> result.notImplemented()
            HandlePurchasesRoutes.fetchRawReceiptInfo.name -> result.notImplemented()
            HandlePurchasesRoutes.validateReceipt.name -> result.notImplemented()
            HandlePurchasesRoutes.appStoreReceipt.name -> result.notImplemented()
        }
    }

    private fun hasActiveSubscription(result: MethodChannel.Result) {
        val isHasActiveSubscription = Apphud.hasActiveSubscription()
        result.success(isHasActiveSubscription)
    }

    private fun subscription(result: MethodChannel.Result) {
        val subscription = Apphud.subscription()
        if (subscription != null) {
            val dict: HashMap<String, Any?> = DataTransformer.subscription(subscription)
            result.success(dict)
        } else {
            result.success(null)
        }
    }

    private fun subscriptions(result: MethodChannel.Result) {
        val subscriptions = Apphud.subscriptions()
        val jsonList: List<HashMap<String, Any?>> = subscriptions.map {
            DataTransformer.subscription(it)
        }

        result.success(jsonList)
    }


    private fun nonRenewingPurchases(result: MethodChannel.Result) {
        val nonRenewingPurchases = Apphud.nonRenewingPurchases()

        val jsonList: List<HashMap<String, Any?>> = nonRenewingPurchases.map {
            DataTransformer.nonRenewingPurchase(it)
        }

        result.success(jsonList)
    }

    private fun isNonRenewingPurchaseActive(productId: String, result: MethodChannel.Result) {
        val isNonRenewingPurchaseActive = Apphud.isNonRenewingPurchaseActive(productId = productId)
        result.success(isNonRenewingPurchaseActive)
    }

    private fun restorePurchases(result: MethodChannel.Result) {
        Apphud.restorePurchases { apphudSubscriptionList, apphudNonRenewingPurchaseList, error ->
            val resultMap = hashMapOf<String, Any?>()

            apphudSubscriptionList?.let {
                val subscriptionJsonList: List<HashMap<String, Any?>> = it.map { s ->
                    DataTransformer.subscription(s)
                }
                resultMap["subscriptions"] = subscriptionJsonList
            }

            apphudNonRenewingPurchaseList?.let {
                val nrPurchasesJsonList: List<HashMap<String, Any?>> = it.map { p ->
                    DataTransformer.nonRenewingPurchase(p)
                }
                resultMap["nrPurchases"] = nrPurchasesJsonList
            }

            error?.let {
                resultMap["error"] = DataTransformer.apphudError(it)
            }
            try {
                result.success(resultMap)
            } catch (e: IllegalStateException ) {
                Log.e("Apphud", e.toString(), e)
            }
        }
    }

    private fun migratePurchasesIfNeeded(result: MethodChannel.Result) {
        // not implemented
    }

    private fun fetchRawReceiptInfo(result: MethodChannel.Result) {
        // not implemented
    }

    class IsNonRenewingPurchaseActiveParser(val result: MethodChannel.Result) {
        fun parse(args: Map<String, Any>?, callback: (productId: String) -> Unit) {
            try {
                args ?: throw IllegalArgumentException("productIdentifier is required argument")
                val productId = args["productIdentifier"] as? String
                        ?: throw IllegalArgumentException("productIdentifier is required argument")

                callback(productId)
            } catch (e: IllegalArgumentException) {
                result.error("400", e.message, "")
            }
        }
    }
}


enum class HandlePurchasesRoutes {
    hasActiveSubscription,
    subscription,
    subscriptions,
    nonRenewingPurchases,
    isNonRenewingPurchaseActive,
    restorePurchases,
    migratePurchasesIfNeeded,
    fetchRawReceiptInfo,
    validateReceipt,
    appStoreReceipt;

    companion object Mapper {
        fun stringValues(): List<String> {
            return values().map { route -> route.toString() }
        }
    }
}