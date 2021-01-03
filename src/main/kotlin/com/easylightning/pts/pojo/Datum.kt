package com.easylightning.pts.pojo

/**
 * A data class for compatibility of the old version.
 */
class Datum(
        var imei: String,
        var time: Long,
        var user: String,
        var password: String,
        var values: Map<String, Float>
) {
}