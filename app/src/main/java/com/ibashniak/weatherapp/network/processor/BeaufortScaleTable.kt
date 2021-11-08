package com.ibashniak.weatherapp.network.processor

class BeaufortScaleTable {

    companion object {
        private fun getBeaufortNumber(speed: Double): Int {
            return when {
                speed <= 0.2 -> 0
                speed <= 1.5 -> 1
                speed <= 3.3 -> 2
                speed <= 5.4 -> 3
                speed <= 7.9 -> 4
                speed <= 10.7 -> 5
                speed <= 13.8 -> 6
                speed <= 17.1 -> 7
                speed <= 20.7 -> 8
                speed <= 24.4 -> 9
                speed <= 28.4 -> 10
                speed <= 32.6 -> 11
                else -> 12
            }
        }


        fun getBeaufortString(speed: Double, beaufortString: Array<String>): String {
            assert (!speed.isNaN())
            assert (speed >= 0)
            assert (beaufortString.size == 13)
            return beaufortString[getBeaufortNumber(speed)]
        }
    }
}