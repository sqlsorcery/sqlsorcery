package com.sqlsorcery

import java.sql.Connection
import java.sql.DriverManager

class Session {
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
}
