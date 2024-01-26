package com.samsung.android.app.foodnote.data

import com.samsung.android.sdk.healthdata.HealthConstants.Nutrition

class DailyIntakeCalories(
    val breakfast: Float,
    val lunch: Float,
    val dinner: Float,
    val morningSnack: Float,
    val afternoonSnack: Float,
    val eveningSnack: Float
) {

    companion object {
        fun fromMap(calorieMap: Map<Int?, Float>): DailyIntakeCalories {
            return DailyIntakeCalories(
                calorieMap[Nutrition.MEAL_TYPE_BREAKFAST] ?: 0f,
                calorieMap[Nutrition.MEAL_TYPE_LUNCH] ?: 0f,
                calorieMap[Nutrition.MEAL_TYPE_DINNER] ?: 0f,
                calorieMap[Nutrition.MEAL_TYPE_MORNING_SNACK] ?: 0f,
                calorieMap[Nutrition.MEAL_TYPE_AFTERNOON_SNACK] ?: 0f,
                calorieMap[Nutrition.MEAL_TYPE_EVENING_SNACK] ?: 0f
            )
        }
    }
}