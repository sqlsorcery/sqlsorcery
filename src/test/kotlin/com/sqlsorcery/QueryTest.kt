package com.sqlsorcery

import org.junit.Assert
import org.junit.Test
import kotlin.collections.*
import kotlin.reflect.KProperty
import kotlin.text.*

abstract class Identifier

class ValueIdentifier<V>(val type: Type<V>, val value: V) : Identifier() {
    override fun toString(): String {
        return "?"
    }
}

class Column<T>(val type: Type<T>) : Identifier() {
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

    infix fun eq(other: T): Condition {
        return EqualCondition(this, ValueIdentifier(type, other))
    }

    internal fun init(name: String, modelTable: ModelTable<*>) {
        _name = name
        _modelTable = modelTable
    }

    override fun toString(): String {
        return aliasedName
    }
}

abstract class Condition : Identifier()

abstract class BinaryCondition(private val left: Identifier, private val right: Identifier, private val operator: String) : Condition() {
    override fun toString(): String {
        return "$left $operator $right"
    }
}

class EqualCondition(left: Identifier, right: Identifier) : BinaryCondition(left, right, "=")

abstract class Query<T : Query.Result>(vararg val fields: ModelTable<*>) {
    private val conditions = arrayListOf<Condition>()

    internal fun addConditions(conditions: List<Condition>) {
        this.conditions.addAll(conditions)
    }

    override fun toString(): String {
        val selectFields = fields.flatMap { it.meta.fields.values }.map { "${it.fullQuallfiedName} AS ${it.aliasedName}" }
        return "SELECT ${selectFields.joinToString(", ")} FROM ${fields.first().meta.name} WHERE ${conditions.map { it.toString() }.joinToString(" AND ")}"
    }

    fun all(): List<T> {
        return listOf<T>().map { mapResult(listOf<Any?>()) }
    }

    fun one(): T {
        return mapResult(listOf<Any?>())
    }

    abstract protected fun mapResult(items: List<*>): T

    abstract class Result(protected val items: List<*>)
}

class Query1<M : Model?>(field: ModelTable<M>) : Query<Query1.Result1<M>>(field) {
    override protected fun mapResult(items: List<*>) = Result1<M>(items)

    class Result1<T1>(items: List<*>) : Result(items) {
        @Suppress("UNCHECKED_CAST")
        operator fun component1() = items[0] as T1
    }
}


fun <Q : Query<*>> Q.filter(vararg conditions: Condition): Q {
    addConditions(conditions.toList())
    return this
}

abstract class ModelTable<M : Model?> {
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

fun <M : Model> nullable(field: ModelTable<M>): ModelTable<M?> {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return field as ModelTable<M?>
}

abstract class Model

abstract class Type<T>

class INTEGER : Type<Int>() {
}

class User : Model() {
    companion object : ModelTable<User>() {
        val id = Column(INTEGER())
    }

    val id by User.id
}


class QueryTest {
    @Test
    fun testQuery() {
        Assert.assertEquals(
                "SELECT user.id AS user_id FROM user WHERE user_id = ?",
                User.query.filter(User.id eq 1).toString()
        )
        val (user: User) = User.query.filter(User.id eq 1).one()
        val (user1: User?) = Query1(nullable(User)).one()
        //                Assert.assertEquals(1, user.id)
    }
}

