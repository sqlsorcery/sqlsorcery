package com.sqlsorcery

import org.junit.Assert
import org.junit.Test
import kotlin.collections.*
import kotlin.reflect.KProperty
import kotlin.text.*

class Column<T>(val type: Type<T>) {
    val aliasedName: String
        get() = fullQuallfiedName.replace(".", "_")
    val fullQuallfiedName: String
        get() = "${modelTable.meta.name}.$name"
    val name: String get() = _name
    val modelTable: ModelTable<*> get() = _modelTable

    lateinit private var _name: String
    lateinit private var _modelTable: ModelTable<*>

    operator fun getValue(model: Model, property: KProperty<*>): Int {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun eq(t: T): Condition {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal fun init(name: String, modelTable: ModelTable<*>) {
        _name = name
        _modelTable = modelTable
    }
}

abstract class Condition

abstract class Query(vararg val fields: ModelTable<*>) {
    private val where = arrayListOf<Condition>()

    internal fun addConditions(conditions: List<Condition>) {
        where.addAll(conditions)
    }

    override fun toString(): String {
        val selectFields = fields.flatMap { it.meta.fields.values }.map { "${it.fullQuallfiedName} AS ${it.aliasedName}" }
        return "SELECT ${selectFields.joinToString(", ")} FROM ${fields.first().meta.name}"
    }
}

class Query1<M : Model>(field: ModelTable<M>) : Query(field) {
    fun one(): M {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


fun <Q : Query> Q.filter(vararg conditions: Condition): Q {
    addConditions(conditions.toList())
    throw UnsupportedOperationException("not impeemented") //To change body of created functions use File | Settings | File Templates.
    return this
}

abstract class ModelTable<M : Model> {
    val query: Query1<M>
        get() = Query1(this)

    val meta by lazy {
        Meta(this)
    }
}

class Meta(val modelTable: ModelTable<*>) {
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
    val name = modelTable.javaClass.name.split(".").last().split("$").first().toLowerCase()
}

abstract class Model

abstract class Type<T>

class INTEGER : Type<Int>() {
}

class User : Model() {
    companion object : ModelTable<User>() {
        val id = Column(INTEGER())
    }

    val id: Int by User.id
}


class QueryTest {
    @Test
    fun testQuery() {
        Assert.assertEquals("SELECT user.id AS user_id FROM user", User.query.toString())

        //        val user = User.query.filter(User.id eq 1).one()
        //        Assert.assertEquals(1, user.id)
    }
}

