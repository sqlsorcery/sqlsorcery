package com.sqlsorcery

import java.sql.Connection
import java.sql.DriverManager
import kotlin.collections.hashMapOf
import kotlin.collections.hashSetOf

class Session {
    val identityMap = hashMapOf<Int, Model>()
    private val models = hashSetOf<Model>()

    val connection: Connection
        get() {
            if (_connection == null) {
                _connection = DriverManager.getConnection("jdbc:h2:mem:test-jooq-tools")
            }
            return _connection!!
        }

    private var _connection: Connection? = null

    fun close() {
        _connection?.close()
        _connection = null
    }

    fun execute(sql: String) {
        connection.prepareStatement(sql).execute()
    }

    fun commit() {
        _connection?.commit()
    }

    fun add(model: Model) {
        models.add(model)
    }
}
