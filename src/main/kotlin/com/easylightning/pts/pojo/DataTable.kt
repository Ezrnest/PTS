package com.easylightning.pts.pojo

import java.time.Instant

data class DataRow(val time: Instant,
                   val data: List<Any?>)

data class DataTable(
        val columns: List<String>,
        val data: List<DataRow>
) {


    companion object {
        val EMPTY = DataTable(emptyList(), emptyList())
    }
}