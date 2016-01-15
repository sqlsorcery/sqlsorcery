package com.sqlsorcery

abstract class ModelTable<M : Model>(constructor: () -> M) : Selectable<M> {
    override val meta by lazy {
        ModelTableMeta(this, constructor)
    }
}