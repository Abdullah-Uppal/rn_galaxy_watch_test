package com.samsung.android.app.foodnote

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.samsung.android.app.foodnote.data.DailyIntakeCalories
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import com.samsung.android.sdk.healthdata.HealthConstants
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthDataStore.ConnectionListener
import com.samsung.android.sdk.healthdata.HealthDataUtil
import com.samsung.android.sdk.healthdata.HealthDeviceManager
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.Closeable
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class FoodDataModule(reactApplicationContext: ReactApplicationContext): ReactContextBaseJavaModule(reactApplicationContext), Closeable {
    private var store: HealthDataStore;
    private var dataHelper: FoodDataHelper

    private val mConnectionListener = object : ConnectionListener {
        override fun onConnected() {
            Log.d("onConnected", "Connected")
        }

        override fun onConnectionFailed(p0: HealthConnectionErrorResult?) {
            Log.d("onConnectionFailed", "Connection Failed")
        }

        override fun onDisconnected() {
            Log.d("onDisconnected", "Disconnected")
        }

    }
    private var mHealthDataResolver: HealthDataResolver
    private var mHealthDeviceManager: HealthDeviceManager

    init {
        store = HealthDataStore(reactApplicationContext.applicationContext,  mConnectionListener);
        store.connectService()
        dataHelper = FoodDataHelper(store, null)
        mHealthDataResolver = HealthDataResolver(store, null)
        mHealthDeviceManager = HealthDeviceManager(store)
    }
    @ReactMethod
    fun getData(st: String, promise: Promise) {
        val startTime = st.toLong()

        val aliasSumOfCalorie = "alias_sum_of_calorie"
        val aliasGroupOfMealType = "alias_group_of_meal_type"
        var result = 0f
        val request = HealthDataResolver.AggregateRequest.Builder()
            .setDataType(HealthConstants.Nutrition.HEALTH_DATA_TYPE)
            .addFunction(
                HealthDataResolver.AggregateRequest.AggregateFunction.SUM,
                HealthConstants.Nutrition.CALORIE,
                aliasSumOfCalorie
            )
            .setLocalTimeRange(
                HealthConstants.Nutrition.START_TIME,
                HealthConstants.Nutrition.TIME_OFFSET,
                startTime,
                startTime + TIME_INTERVAL
            )
            .addGroup(HealthConstants.Nutrition.MEAL_TYPE, aliasGroupOfMealType)
            .build()
        mHealthDataResolver.aggregate(request).setResultListener { res ->

            val params = Arguments.createMap().apply {
                putString("calories", res.resultCursor.getString(0))
            }
            sendEvent(reactApplicationContext, "Calories", params)
//           promise.resolve(10)
        }

//        return Single.fromCallable {
//            val request: HealthDataResolver.AggregateRequest = HealthDataResolver.AggregateRequest.Builder()
//                .setDataType(HealthConstants.Nutrition.HEALTH_DATA_TYPE)
//                .addFunction(
//                    HealthDataResolver.AggregateRequest.AggregateFunction.SUM,
//                    HealthConstants.Nutrition.CALORIE,
//                    aliasSumOfCalorie
//                )
//                .setLocalTimeRange(
//                    HealthConstants.Nutrition.START_TIME,
//                    HealthConstants.Nutrition.TIME_OFFSET,
//                    startTime,
//                    startTime + TIME_INTERVAL
//                )
//                .addGroup(HealthConstants.Nutrition.MEAL_TYPE, aliasGroupOfMealType)
//                .build()
//            mHealthDataResolver.aggregate(request).await()
//        }
//            .doAfterSuccess(HealthDataResolver.AggregateResult::close)
//            .flattenAsObservable { result -> result }
//            .toMap({ data -> data.getInt(aliasGroupOfMealType) }
//            ) { data -> data.getFloat(aliasSumOfCalorie) }
//            .map(DailyIntakeCalories::fromMap)
    }
    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
    }
    @ReactMethod
    fun addListener(eventName: String) {
    }
    @ReactMethod
    fun removeListeners(count: Int) {

    }


    companion object {
        private val TAG: String = MainActivity.TAG
        private val TIME_INTERVAL = TimeUnit.DAYS.toMillis(1)
    }


    override fun getName() = "FoodDataModule"
    override fun close() {
        store.disconnectService()
    }


}
