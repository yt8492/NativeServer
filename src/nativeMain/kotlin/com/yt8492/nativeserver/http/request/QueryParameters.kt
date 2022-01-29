package com.yt8492.nativeserver.http.request

import com.yt8492.nativeserver.util.URLDecoder

class QueryParameters(
    rawQueryParameters: String,
) : Iterable<Query> {
    private val queries = if (rawQueryParameters.isNotEmpty()) {
        rawQueryParameters.split("&").map { rawQuery ->
            rawQuery.split("=").let {
                val name = URLDecoder.decode(it[0])
                val value = URLDecoder.decode(it[1])
                Query(name, value)
            }
        }
    } else {
        listOf()
    }

    operator fun get(name: String): String? {
        return queries.firstOrNull { it.name == name }
            ?.value
    }

    fun getAll(name: String): List<String> {
        return queries.asSequence()
            .filter { it.name == name }
            .map { it.value }
            .toList()
    }

    override fun iterator(): Iterator<Query> {
        return queries.iterator()
    }
}
