package com.sqlsorcery

import com.sqlsorcery.types.Type
import kotlin.collections.listOf

class ValueIdentifier<V>(val type: Type<V>, val value: V) : Identifier() {
    override fun toString(): String {
        return "?"
    }

    override val params: List<Any?>
        get() = listOf(if (value == null) value else type.encode(value))
}