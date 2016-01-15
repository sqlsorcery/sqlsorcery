package com.sqlsorcery.kotlin

import com.sqlsorcery.*
import com.sqlsorcery.queries.query
import com.sqlsorcery.types.INTEGER
import com.sqlsorcery.types.STRING
import org.junit.Assert
import org.junit.Test


class ImplementationTest {
    class User : Model() {
        companion object : ModelTable<User>(::User) {
            val id = Column(INTEGER())
            val name = Column(STRING(length = 50))
        }

        val id by User.id
        val name by User.name
    }

    @Test
    fun test() {
        val db = SQLSorcery()
        db.transaction { session ->
            session.execute("create table user (id INT, name VARCHAR(50));")
            session.execute("insert into user values (1, 'john')")
            session.execute("insert into user values (2, 'bill')")

            val users = session.query(User).all()
            Assert.assertEquals(1, users[0].component1().id)
            Assert.assertEquals("john", users[0].component1().name)
            Assert.assertEquals(2, users[1].component1().id)
            Assert.assertEquals("bill", users[1].component1().name)

            val (user) = session.query(User).filter(User.id eq 1).one()
            Assert.assertEquals(1, user.id)
            Assert.assertEquals("john", user.name)
        }
    }
}

