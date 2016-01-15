package com.sqlsorcery

import kotlin.collections.hashMapOf

abstract class Model {
    internal val meta = Meta()

    class Meta {
        internal val map = hashMapOf<String, Any?>()
    }
}