package com.sqlsorcery

import kotlin.collections.plus

abstract class BinaryCondition(private val left: Identifier, private val right: Identifier, private val operator: String) : Condition() {
    override fun toString(): String {
        return "$left $operator $right"
    }

    override val params: List<Any?>
        get() = left.params + right.params
}