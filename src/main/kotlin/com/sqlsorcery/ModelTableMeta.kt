package com.sqlsorcery

import kotlin.collections.*
import kotlin.text.split
import kotlin.text.startsWith
import kotlin.text.substring
import kotlin.text.toLowerCase

class ModelTableMeta<M : Model>(val modelTable: ModelTable<M>, private val modelConstructor: () -> M) : Selectable.Meta, Table {
    override val aliasedName: String get() = fullQualifiedName

    override val fullQualifiedName: String = modelTable.javaClass.name.extractTargetClassName().toLowerCase()

    override fun map(func: () -> Any?): Any? {
        val model = modelConstructor()
        fields.values.forEach {
            model.meta.map.put(it.name, it.type.decode(func()))
        }
        return model
    }

    val fields = run {
        modelTable.javaClass.methods
                .filter { it.parameterCount == 0 }
                .filter { it.name.startsWith("get") }
                .filter { Column::class.java.isAssignableFrom(it.returnType) }
                .map {
                    val name = it.name.substring(3, 4).toLowerCase() + it.name.substring(4)
                    val column = it.invoke(modelTable) as Column<*>
                    column.init(name, modelTable)
                    name to column
                }
                .toMap()
    }

    override val flatten: List<Selectable<*>> = fields.values.toList()

    override val table: Table?
        get() = this
}

internal fun String.extractTargetClassName(): String {
    return split(".").last().split("$").last { it != "Companion" }
}