package com.sqlsorcery

import kotlin.collections.listOf


interface Selectable<T> {
    val meta: Meta

    interface Meta {
        val params: List<Any?>
            get() = listOf()
        val flatten: List<Selectable<*>>
        val fullQualifiedName: String
        val table: Table?
            get() = null

        fun map(session: Session, func: () -> Any?): Any?
    }
}
