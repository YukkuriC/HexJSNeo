package io.yukkuric.hexjsneo.ext

object Regexxx {
    val REGEX_PATTERN = Regex("(?:(east|west|north_east|north_west|south_east|south_west),\\s*)?([wedsaq]+)")
    val REGEX_PATTERN_REV = Regex("([wedsaq]+),\\s*(east|west|north_east|north_west|south_east|south_west)")
}