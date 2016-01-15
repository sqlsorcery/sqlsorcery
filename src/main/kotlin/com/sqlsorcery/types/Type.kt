package com.sqlsorcery.types

abstract class Type<T> {
    fun encode(value: T): Any? = value

    fun decode(it: Any?): Any? = it
}