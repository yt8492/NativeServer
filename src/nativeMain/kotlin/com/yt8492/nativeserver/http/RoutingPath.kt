package com.yt8492.nativeserver.http

import com.yt8492.nativeserver.http.request.PathParameters

class RoutingPath(
    private val parts: List<RoutingPathSegment>,
) {
    fun evaluate(path: String): PathEvaluationResult {
        val pathParts = path.splitToSequence("/")
            .filter { it.isNotBlank() }
            .toList()
        if (pathParts.size != parts.size) {
            return PathEvaluationResult.failed()
        }
        var quality = PathEvaluationResult.QUALITY_CONSTANT
        val parameters = mutableMapOf<String, String>()
        pathParts.zip(parts).forEach { (pathPart, part) ->
            when (part) {
                is RoutingPathSegment.Constant -> {
                    if (part.value != pathPart) {
                        return PathEvaluationResult.failed()
                    }
                }
                is RoutingPathSegment.Parameter -> {
                    quality *= PathEvaluationResult.QUALITY_PARAMETER
                    parameters[part.value] = pathPart
                }
                is RoutingPathSegment.WildCard -> {
                    quality *= PathEvaluationResult.QUALITY_WILDCARD
                }
            }
        }
        val pathParameters = PathParameters(parameters)
        return PathEvaluationResult(
            true,
            quality,
            pathParameters,
        )
    }

    companion object {
        private val ROOT: RoutingPath = RoutingPath(listOf())

        fun parse(path: String): RoutingPath {
            if (path == "/") return ROOT
            val parts = path.splitToSequence("/")
                .filter { it.isNotBlank() }
                .map {
                    when {
                        it == "*" -> RoutingPathSegment.WildCard
                        it.startsWith(":") -> RoutingPathSegment.Parameter(it.drop(1))
                        else -> RoutingPathSegment.Constant(it)
                    }
                }
                .toList()
            return RoutingPath(parts)
        }
    }

    override fun toString(): String = parts.joinToString("/", "/") { it.value }
}

sealed interface RoutingPathSegment {
    val value: String

    data class Constant(override val value: String) : RoutingPathSegment
    data class Parameter(override val value: String) : RoutingPathSegment
    object WildCard : RoutingPathSegment {
        override val value: String = "*"
    }
}

data class PathEvaluationResult(
    val succeeded: Boolean,
    val quality: Double,
    val parameters: PathParameters,
) {
    companion object {
        const val QUALITY_CONSTANT = 1.0
        const val QUALITY_PARAMETER = 0.8
        const val QUALITY_WILDCARD = 0.5
        const val QUALITY_FAILED = 0.0

        fun failed(): PathEvaluationResult {
            return PathEvaluationResult(
                succeeded = false,
                quality = QUALITY_FAILED,
                PathParameters(emptyMap()),
            )
        }
    }
}
