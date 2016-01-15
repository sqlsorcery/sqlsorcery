package com.sqlsorcery

import kotlin.collections.listOf

abstract class Identifier {
    open val params: List<Any?>
        get() = listOf()
}