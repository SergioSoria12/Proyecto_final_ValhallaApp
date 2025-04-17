package edu.sergiosoria.valhallathebox.utils

import edu.sergiosoria.valhallathebox.R

fun getWodImageRes(nombre: String?): Int {
    return when (nombre) {
        "WOD 1" -> R.drawable.wod1
        "WOD 2" -> R.drawable.wod2
        "WOD 3" -> R.drawable.wod3
        "WOD 4" -> R.drawable.wod4
        "WOD 5" -> R.drawable.wod5
        "WOD 6" -> R.drawable.wod6
        "WOD 7" -> R.drawable.wod7
        "WOD 8" -> R.drawable.wod8
        "WOD 9" -> R.drawable.wod9
        "WOD 10" -> R.drawable.wod10
        "WOD 11" -> R.drawable.wod11
        "WOD 12" -> R.drawable.wod12
        else -> R.drawable.wod1
    }
}
