/*
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 * <p>
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 * <p>
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */
package com.samsung.android.app.foodnote

import android.os.Handler
import com.samsung.android.app.foodnote.data.DailyIntakeCalories
import com.samsung.android.app.foodnote.data.DailyIntakeDetails
import com.samsung.android.sdk.healthdata.HealthConstants.Nutrition
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateResult
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthDeviceManager
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

class FoodDataHelper(healthDataStore: HealthDataStore?, resultProcessingHandler: Handler?) {
    private val mHealthDataResolver: HealthDataResolver
    private val mHealthDeviceManager: HealthDeviceManager

    init {
        mHealthDataResolver = HealthDataResolver(healthDataStore, resultProcessingHandler)
        mHealthDeviceManager = HealthDeviceManager(healthDataStore)
    }
    fun readDailyIntakeCalories(startTime: Long): Single<DailyIntakeCalories> {
        val aliasSumOfCalorie = "alias_sum_of_calorie"
        val aliasGroupOfMealType = "alias_group_of_meal_type"
        return Single.fromCallable {
            val request: AggregateRequest = AggregateRequest.Builder()
                .setDataType(Nutrition.HEALTH_DATA_TYPE)
                .addFunction(
                    AggregateRequest.AggregateFunction.SUM,
                    Nutrition.CALORIE,
                    aliasSumOfCalorie
                )
                .setLocalTimeRange(
                    Nutrition.START_TIME,
                    Nutrition.TIME_OFFSET,
                    startTime,
                    startTime + TIME_INTERVAL
                )
                .addGroup(Nutrition.MEAL_TYPE, aliasGroupOfMealType)
                .build()
            mHealthDataResolver.aggregate(request).await()
        }
            .doAfterSuccess(AggregateResult::close)
            .flattenAsObservable { result -> result }
            .toMap({ data -> data.getInt(aliasGroupOfMealType) }
            ) { data -> data.getFloat(aliasSumOfCalorie) }
            .map(DailyIntakeCalories::fromMap)
    }

    fun readDailyIntakeDetails(startTime: Long, mealType: Int): Single<DailyIntakeDetails> {
        return Single.fromCallable {

            // Read the foodIntake data of specified day and meal type(startTime to end)
            val request: HealthDataResolver.ReadRequest =
                HealthDataResolver.ReadRequest.Builder().setDataType(Nutrition.HEALTH_DATA_TYPE)
                    .setProperties(
                        arrayOf<String>(
                            Nutrition.UUID,
                            Nutrition.TITLE,
                            Nutrition.CALORIE,
                            Nutrition.PACKAGE_NAME
                        )
                    )
                    .setLocalTimeRange(
                        Nutrition.START_TIME,
                        Nutrition.TIME_OFFSET,
                        startTime,
                        startTime + TIME_INTERVAL
                    )
                    .setFilter(HealthDataResolver.Filter.eq<Int>(Nutrition.MEAL_TYPE, mealType))
                    .build()
            mHealthDataResolver.read(request).await()
        }
            .doAfterSuccess(HealthDataResolver.ReadResult::close)
            .flattenAsObservable { result -> result }
            .reduce(DailyIntakeDetails()) { dailyIntakeDetails, data ->
                val uuid: String = data.getString(Nutrition.UUID)
                val calories: Float = data.getFloat(Nutrition.CALORIE)
                val title: String = data.getString(Nutrition.TITLE)
                val packageName: String = data.getString(Nutrition.PACKAGE_NAME)
                dailyIntakeDetails.addCalories(calories)
                dailyIntakeDetails.uuidList.add(uuid)
                dailyIntakeDetails.foodNameList.add(
                    title + " : (" + calories + " Cals"
                            + if (BuildConfig.APPLICATION_ID == packageName) ")" else ", Not Deletable)"
                )
                dailyIntakeDetails
            }
    }

    companion object {
        private val TAG: String = MainActivity.TAG
        private val TIME_INTERVAL = TimeUnit.DAYS.toMillis(1)
    }
}
