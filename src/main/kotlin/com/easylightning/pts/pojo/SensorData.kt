package com.easylightning.pts.pojo

class SensorData(
        username : String,
        password : String,
        imei : String,
        sensors : List<String>,
        val times : List<Long>,
        /**
         * A 2d list containing all the sensor values.
         *
         * size = `times.size` * `sensors.size`
         */
        val values : List<List<Float?>> // list of rows
) : SensorInfo(username,password, imei, sensors) {

    fun checkDataValid() : Boolean{
        if (values.size != times.size) {
            return false
        }
        val n = sensors.size
        return values.all { it.size == n }
    }

    companion object{
        val EMPTY = SensorData("","","", emptyList(), emptyList(), emptyList())
    }
}
