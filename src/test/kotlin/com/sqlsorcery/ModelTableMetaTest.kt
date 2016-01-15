package com.sqlsorcery

import org.junit.Assert
import org.junit.Test

class ModelTableMetaTest {
    class User : Model() {
        companion object : ModelTable<User>(::User) {
        }
    }

    @Test
    fun testFullQualifiedName() {
        Assert.assertEquals(
                "User", "com.sqlsorcery.User".extractTargetClassName()
        )
        Assert.assertEquals(
                "User", "com.sqlsorcery.User\$Companion".extractTargetClassName()
        )
        Assert.assertEquals(
                "User", "com.sqlsorcery.ModelTableMetaTest\$User\$Companion".extractTargetClassName()
        )
        Assert.assertEquals("user", User.meta.fullQualifiedName)
    }
}