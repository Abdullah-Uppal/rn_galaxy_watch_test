package com.samsung.android.app.foodnote.data

class FoodInfo(
    val title: String,
    @JvmField val calorie: Float,
    @JvmField val totalFat: Float,
    @JvmField val saturatedFat: Float,
    @JvmField val polysaturatedFat: Float,
    @JvmField val monosaturatedFat: Float,
    @JvmField val transFat: Float,
    @JvmField val carbohydrate: Float,
    @JvmField val dietaryFiber: Float,
    @JvmField val sugar: Float,
    @JvmField val protein: Float,
    val unitCountPerCalorie: Float,
    @JvmField val cholesterol: Float,
    @JvmField val soduim: Float,
    @JvmField val potassium: Float,
    @JvmField val vitaminA: Float,
    @JvmField val vitaminC: Float,
    @JvmField val calcium: Float,
    @JvmField val iron: Float
)
