package com.sqlsorcery

import java.util.*
import kotlin.collections.*

open class Query<R : ResultSet>(vararg selectableList: Selectable<*>) {
    lateinit var session: Session
    private var conditions = arrayListOf<Condition>()
    private var selected = selectableList.toList()
    private val selectFields: List<Selectable<*>>
        get() = selected.flatMap { it.meta.flatten }

    fun addConditions(conditions: List<Condition>) {
        this.conditions.addAll(conditions)
    }

    fun all(): List<R> {
        val results = arrayListOf<R>()
        val statement = session.connection.prepareStatement(render())
        renderParams().forEachIndexed { i, param -> statement.setObject(i + 1, param) }
        statement.execute()
        statement.resultSet.apply {
            while (next()) {
                var index = 0
                val result = arrayListOf<Any?>()
                selected.forEach {
                    result.add(it.meta.map(session) { getObject(++index) })
                }
                results.add(buildResult(result))
            }
        }
        return results
    }

    open protected fun buildResult(result: ArrayList<Any?>): R {
        @Suppress("UNCHECKED_CAST")
        return ResultSet(result) as R
    }

    private fun renderParams() = selectFields.flatMap { it.meta.params } + conditions.flatMap { it.params }

    fun one(): R {
        val results = all()
        if (results.size != 1) {
            // TODO change correct exception
            throw IllegalArgumentException()
        }
        return results[0]
    }

    fun render(): String {
        var query = "SELECT ${selectFields.map { it.meta.fullQualifiedName }.joinToString(", ")}"
        selectFields
                .map { it.meta.table }
                .filterNotNull()
                .map { it.aliasedName }
                .toSet()
                .apply {
                    val tables = if (isEmpty()) {
                        "DUAL"
                    } else {
                        joinToString(", ")
                    }
                    query += " FROM $tables"
                }
        if (conditions.isNotEmpty()) {
            query += " WHERE ${conditions.map { it.toString() }.joinToString(", ")}"
        }
        return query
    }
}

open class ResultSet(items: List<Any?>) : List<Any?> by items

fun <T : Query<*>> T.filter(vararg conditions: Condition): T {
    addConditions(conditions.toList())
    return this
}