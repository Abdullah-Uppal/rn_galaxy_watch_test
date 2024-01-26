
package com.samsung.android.app.foodnote.data

class DailyIntakeDetails {
    var calories = 0f
        private set
    val uuidList: ArrayList<String> = ArrayList()
    val foodNameList: ArrayList<String> = ArrayList()
    fun addCalories(calories: Float) {
        this.calories += calories
    }
}
