package com.sqlsorcery

import java.util.*
import kotlin.collections.hashMapOf
import kotlin.collections.map
import kotlin.collections.toTypedArray

abstract class Model {
    internal val meta = Meta()

    class Meta {
        internal lateinit var tableMeta: ModelTableMeta<*>
        internal val map = hashMapOf<String, Any?>()
        internal val id: Int
            get() = Objects.hash(*tableMeta.primaryKeys.map { map[it.name] }.toTypedArray())
    }
}