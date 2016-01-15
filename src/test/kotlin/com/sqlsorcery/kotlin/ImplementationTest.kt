package com.sqlsorcery.kotlin

import com.sqlsorcery.*
import com.sqlsorcery.queries.query
import com.sqlsorcery.types.INTEGER
import com.sqlsorcery.types.STRING
import org.junit.Assert
import org.junit.Test
import kotlin.collections.first
import kotlin.collections.map


class ImplementationTest {
    class User : Model() {
        companion object : ModelTable<User>(::User) {
            val id = Column(INTEGER())
            val name = Column(STRING(length = 50))

            init {
                meta.primaryKeys = arrayOf(id)
            }
        }

        var id by User.id
        var name by User.name
    }

    @Test
    fun test() {
        val db = SQLSorcery()
        db.transaction { session ->
            session.execute("create table user (id INT, name VARCHAR(50));")
            session.add(User().apply { id = 1; name = "john" })
            session.add(User().apply { id = 2; name = "bill" })
            session.commit()

            val users = session.query(User).all().map { it.component1() }
            Assert.assertEquals(1, users[0].id)
            Assert.assertEquals("john", users[0].name)
            Assert.assertEquals(2, users[1].id)
            Assert.assertEquals("bill", users[1].name)

            val (user) = session.query(User).filter(User.id eq 1).one()
            Assert.assertEquals(1, user.id)
            Assert.assertEquals("john", user.name)

            // check identity map
            Assert.assertEquals(users.first { it.id == user.id }, user)
        }
    }
}

