package com.sqlsorcery

import com.sqlsorcery.types.Type
import kotlin.collections.listOf
import kotlin.reflect.KProperty
import kotlin.text.replace

class Column<T>(val type: Type<T>) : Identifier(), Selectable<T> {
    override val meta: Selectable.Meta = Meta(this)

    val aliasedName: String
        get() = fullQualifiedName.replace(".", "_")
    val fullQualifiedName: String
        get() = "${modelTable.meta.fullQualifiedName}.$name"
    val name: String get() = _name
    val modelTable: ModelTable<*> get() = _modelTable

    lateinit private var _name: String
    lateinit private var _modelTable: ModelTable<*>

    operator fun getValue(model: Model, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return model.meta.map[name] as T
    }

    infix fun eq(other: T): com.sqlsorcery.Condition {
        return com.sqlsorcery.EqualCondition(this, com.sqlsorcery.ValueIdentifier(type, other))
    }

    internal fun init(name: String, modelTable: ModelTable<*>) {
        _name = name
        _modelTable = modelTable
    }

    override fun toString(): String {
        return fullQualifiedName
    }

    fun nullable(): Column<T?> {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return this as Column<T?>
    }

    class Meta<T>(val column: Column<T>) : Selectable.Meta {
        override val fullQualifiedName: String
            get() = column.fullQualifiedName

        override fun map(func: () -> Any?): Any? {
            return column.type.decode(func())
        }

        override val flatten: List<Selectable<*>> = listOf(column)
        override val table: Table? get() = column.modelTable.meta
    }
}