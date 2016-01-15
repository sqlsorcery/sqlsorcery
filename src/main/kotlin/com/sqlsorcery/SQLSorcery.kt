package com.sqlsorcery

class SQLSorcery {
    fun transaction(func: (session: Session) -> Unit) {
        val session = createSession()
        try {
            func(session)
        } finally {
            session.close()
        }
    }

    fun createSession(): Session {
        return Session()
    }
}