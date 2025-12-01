package com.skash.sweeper.domain.model

import java.io.File

data class PathSegment(
    val name: String,
    val path: String
)

fun parsePathToSegments(path: String): List<PathSegment> {
    val separator = File.separatorChar
    val segments = mutableListOf<PathSegment>()

    val root = File(path).toPath().root?.toString() ?: separator.toString()

    segments.add(PathSegment(if (root == "/") "/" else root.trimEnd(separator), root))

    var currentBuild = root
    val rawSegments = path.split(separator).filter { it.isNotEmpty() && it != "/" && it != root.trimEnd(separator) }

    rawSegments.forEach { part ->
        currentBuild = if (currentBuild.endsWith(separator)) "$currentBuild$part" else "$currentBuild$separator$part"
        segments.add(PathSegment(part, currentBuild))
    }

    return segments
}