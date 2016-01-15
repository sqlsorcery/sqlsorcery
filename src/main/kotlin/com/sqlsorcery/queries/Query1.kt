package com.sqlsorcery.queries

import com.sqlsorcery.Query
import com.sqlsorcery.ResultSet
import com.sqlsorcery.Selectable
import com.sqlsorcery.Session
import java.util.*

class Query1<T1>(s1: Selectable<T1>) : Query<ResultSet1<T1>>(s1) {
    override fun buildResult(result: ArrayList<Any?>): ResultSet1<T1> {
        return ResultSet1(result)
    }
}

class ResultSet1<T1>(items: List<Any?>) : ResultSet(items) {
    @Suppress("UNCHECKED_CAST")
    operator fun component1(): T1 = this[0] as T1
}

fun <T> Session.query(s1: Selectable<T>): Query1<T> {
    return Query1(s1).apply { session = this@query }
}