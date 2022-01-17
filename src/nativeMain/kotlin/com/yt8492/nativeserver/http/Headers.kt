package com.yt8492.nativeserver.http

class Headers : Iterable<Header> {
    private val headers = mutableListOf<Header>()

    operator fun get(name: String): String? {
        return headers.firstOrNull {
            it.name.equals(name, ignoreCase = true)
        }?.value
    }

    fun getAll(name: String): List<String> {
        return headers.asSequence()
            .filter { it.name.equals(name, ignoreCase = true) }
            .map { it.value }
            .toList()
    }

    fun add(name: String, value: String) {
        headers.add(Header(name, value))
    }

    fun add(name: String, values: List<String>) {
        headers.addAll(values.map { Header(name, it) })
    }

    override fun iterator(): Iterator<Header> {
        return headers.iterator()
    }
}
