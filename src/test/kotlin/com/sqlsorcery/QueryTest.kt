package com.sqlsorcery

import org.h2.Driver
import org.junit.Assert
import org.junit.Test
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.collections.*
import kotlin.reflect.KProperty
import kotlin.text.*

abstract class Identifier {
    open val params: List<Param<*>>
        get() = listOf()
}

class Param<V>(val type: Type<V>, val value: V) {
    fun set(i: Int, statement: PreparedStatement) {
        type.set(i, statement, value)
    }
}

class ValueIdentifier<V>(val type: Type<V>, val value: V) : Identifier() {
    override fun toString(): String {
        return "?"
    }

    override val params: List<Param<*>>
        get() = listOf(Param(type, value))
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

    operator fun getValue(model: Model, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return model.meta.map[name] as T
    }

    infix fun eq(other: T): Condition {
        return EqualCondition(this, ValueIdentifier(type, other))
    }

    internal fun init(name: String, modelTable: ModelTable<*>) {
        _name = name
        _modelTable = modelTable
    }

    override fun toString(): String {
        return fullQuallfiedName
    }

    fun get(resultSet: ResultSet): T = type.get(resultSet, aliasedName)

    fun nullable(): Column<T?> {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return this as Column<T?>
    }
}

abstract class Condition : Identifier()

abstract class BinaryCondition(private val left: Identifier, private val right: Identifier, private val operator: String) : Condition() {
    override fun toString(): String {
        return "$left $operator $right"
    }

    override val params: List<Param<*>>
        get() = left.params + right.params
}

class EqualCondition(left: Identifier, right: Identifier) : BinaryCondition(left, right, "=")

abstract class Query<T : Query.Result>(vararg val fields: ModelTable<*>) {
    private val conditions = arrayListOf<Condition>()

    private val params: List<Param<*>>
        get() {
            return conditions.flatMap { it.params }
        }

    internal fun addConditions(conditions: List<Condition>) {
        this.conditions.addAll(conditions)
    }

    override fun toString(): String {
        val selectFields = fields.flatMap { it.meta.fields.values }.map { "${it.fullQuallfiedName} AS ${it.aliasedName}" }
        return "SELECT ${selectFields.joinToString(", ")} FROM ${fields.first().meta.name} WHERE ${conditions.map { it.toString() }.joinToString(" AND ")}"
    }

    fun all(conn: Connection): List<T> {
        val statement = conn.prepareStatement(toString())
        params.forEachIndexed { i, param -> param.set(i + 1, statement) }
        statement.execute()
        val resultSet = statement.resultSet
        val results = arrayListOf<T>()
        while (resultSet.next()) {
            results.add(mapResult(fields.map { it.meta.map(resultSet) }))
        }
        return results
    }

    fun one(conn: Connection): T {
        return all(conn)[0]
    }

    abstract protected fun mapResult(items: List<*>): T

    abstract class Result(protected val items: List<*>)
}

class Query1<M : Model>(field: ModelTable<M>) : Query<Query1.Result1<M>>(field) {
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

abstract class ModelTable<M : Model>(constructor: () -> M) {
    val query: Query1<M>
        get() = Query1(this)

    val meta by lazy {
        Meta(this, constructor)
    }
}

class Meta<M : Model>(val modelTable: ModelTable<M>, private val modelConstructor: () -> M) {
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

    fun map(resultSet: ResultSet): M {
        val model = modelConstructor()
        fields.forEach {
            model.meta.map.put(it.key, it.value.get(resultSet))
        }
        return model
    }
}

abstract class Model {
    internal val meta = Meta()

    class Meta {
        internal val map = hashMapOf<String, Any?>()
    }
}

abstract class Type<T> {
    abstract fun set(i: Int, statement: PreparedStatement, value: T)

    @Suppress("UNCHECKED_CAST")
    fun get(resultSet: ResultSet, name: String) = resultSet.getObject(name) as T
}

class INTEGER : Type<Int>() {
    override fun set(i: Int, statement: PreparedStatement, value: Int) {
        statement.setInt(i, value)
    }
}

class User : Model() {
    companion object : ModelTable<User>(::User) {
        val id = Column(INTEGER())
        val no = Column(INTEGER()).nullable()
    }

    val id by User.id
    val no by User.no
}


class QueryTest {
    fun db(run: (conn: Connection) -> Unit) {
        val conn = Driver().connect("jdbc:h2:mem:test-jooq-tools", null)
        try {
            conn.createStatement().apply {
                execute("create table user (id int not null, no int)")
                execute("insert into user values (1, null)")
            }
            run(conn)
        } finally {
            conn.close()
        }
    }

    @Test
    fun testQuery() {
        db {
            val (user) = User.query.filter(User.id eq 1).one(it)
            Assert.assertEquals(1, user.id)
            Assert.assertNull(user.no)
        }
    }
}

