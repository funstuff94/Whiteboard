package com.droid.whiteboard.data

import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke
import com.droid.whiteboard.model.Whiteboard
import com.droid.whiteboard.model.WhiteboardMetadata

/**
 * Pure Kotlin lightweight JSON serializer and parser for Whiteboard data.
 * Zero external dependencies, fully deterministic, works identically on Android and JVM.
 */
object WhiteboardJson {

    fun escape(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun whiteboardToJson(board: Whiteboard): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"id\": \"").append(escape(board.id)).append("\",\n")
        sb.append("  \"title\": \"").append(escape(board.title)).append("\",\n")
        sb.append("  \"createdAt\": ").append(board.createdAt).append(",\n")
        sb.append("  \"updatedAt\": ").append(board.updatedAt).append(",\n")
        sb.append("  \"isDarkMode\": ").append(board.isDarkMode).append(",\n")
        sb.append("  \"strokes\": [\n")
        for (i in board.strokes.indices) {
            val stroke = board.strokes[i]
            sb.append("    {\n")
            sb.append("      \"colorHex\": ").append(stroke.colorHex).append(",\n")
            sb.append("      \"strokeWidth\": ").append(stroke.strokeWidth).append(",\n")
            sb.append("      \"isEraser\": ").append(stroke.isEraser).append(",\n")
            sb.append("      \"points\": [")
            for (j in stroke.points.indices) {
                val pt = stroke.points[j]
                sb.append("{\"x\":").append(pt.x).append(",\"y\":").append(pt.y).append("}")
                if (j < stroke.points.size - 1) sb.append(",")
            }
            sb.append("]\n")
            sb.append("    }")
            if (i < board.strokes.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    fun indexToJson(list: List<WhiteboardMetadata>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        for (i in list.indices) {
            val item = list[i]
            sb.append("  {\n")
            sb.append("    \"id\": \"").append(escape(item.id)).append("\",\n")
            sb.append("    \"title\": \"").append(escape(item.title)).append("\",\n")
            sb.append("    \"createdAt\": ").append(item.createdAt).append(",\n")
            sb.append("    \"updatedAt\": ").append(item.updatedAt).append(",\n")
            sb.append("    \"isDarkMode\": ").append(item.isDarkMode).append(",\n")
            sb.append("    \"strokeCount\": ").append(item.strokeCount).append("\n")
            sb.append("  }")
            if (i < list.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun parseIndex(jsonStr: String): List<WhiteboardMetadata> {
        val parsed = JsonParser(jsonStr).parse() as? List<*> ?: return emptyList()
        val result = mutableListOf<WhiteboardMetadata>()
        for (elem in parsed) {
            val map = elem as? Map<*, *> ?: continue
            val id = map["id"]?.toString() ?: continue
            val title = map["title"]?.toString() ?: "Untitled"
            val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val isDarkMode = map["isDarkMode"] as? Boolean ?: true
            val strokeCount = (map["strokeCount"] as? Number)?.toInt() ?: 0
            result.add(WhiteboardMetadata(id, title, createdAt, updatedAt, isDarkMode, strokeCount))
        }
        return result
    }

    fun parseWhiteboard(jsonStr: String): Whiteboard? {
        val map = JsonParser(jsonStr).parse() as? Map<*, *> ?: return null
        val id = map["id"]?.toString() ?: return null
        val title = map["title"]?.toString() ?: "Untitled"
        val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val isDarkMode = map["isDarkMode"] as? Boolean ?: true

        val strokesRaw = map["strokes"] as? List<*> ?: emptyList<Any>()
        val strokes = mutableListOf<Stroke>()
        for (sElem in strokesRaw) {
            val sMap = sElem as? Map<*, *> ?: continue
            val colorHex = (sMap["colorHex"] as? Number)?.toLong() ?: 0xFFFFFFFF
            val strokeWidth = (sMap["strokeWidth"] as? Number)?.toFloat() ?: 6f
            val isEraser = sMap["isEraser"] as? Boolean ?: false

            val pointsRaw = sMap["points"] as? List<*> ?: emptyList<Any>()
            val points = mutableListOf<Point>()
            for (pElem in pointsRaw) {
                val pMap = pElem as? Map<*, *> ?: continue
                val x = (pMap["x"] as? Number)?.toFloat() ?: 0f
                val y = (pMap["y"] as? Number)?.toFloat() ?: 0f
                points.add(Point(x, y))
            }
            strokes.add(Stroke(points, colorHex, strokeWidth, isEraser))
        }

        return Whiteboard(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDarkMode = isDarkMode,
            strokes = strokes
        )
    }

    private class JsonParser(private val src: String) {
        private var idx = 0

        fun parse(): Any? {
            skipWhitespace()
            if (idx >= src.length) return null
            return parseValue()
        }

        private fun parseValue(): Any? {
            skipWhitespace()
            if (idx >= src.length) return null
            return when (src[idx]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> parseNumber()
            }
        }

        private fun parseObject(): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            idx++ // skip '{'
            skipWhitespace()
            if (idx < src.length && src[idx] == '}') {
                idx++
                return map
            }
            while (idx < src.length) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                if (idx < src.length && src[idx] == ':') {
                    idx++
                }
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (idx < src.length && src[idx] == ',') {
                    idx++
                } else if (idx < src.length && src[idx] == '}') {
                    idx++
                    break
                }
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            val list = mutableListOf<Any?>()
            idx++ // skip '['
            skipWhitespace()
            if (idx < src.length && src[idx] == ']') {
                idx++
                return list
            }
            while (idx < src.length) {
                val value = parseValue()
                list.add(value)
                skipWhitespace()
                if (idx < src.length && src[idx] == ',') {
                    idx++
                } else if (idx < src.length && src[idx] == ']') {
                    idx++
                    break
                }
            }
            return list
        }

        private fun parseString(): String {
            if (src[idx] == '"') idx++
            val sb = StringBuilder()
            while (idx < src.length) {
                val c = src[idx++]
                if (c == '"') break
                if (c == '\\' && idx < src.length) {
                    when (val esc = src[idx++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000c')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        else -> sb.append(esc)
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseNumber(): Number {
            val start = idx
            if (idx < src.length && (src[idx] == '-' || src[idx] == '+')) idx++
            while (idx < src.length && (src[idx].isDigit() || src[idx] == '.' || src[idx] == 'e' || src[idx] == 'E' || src[idx] == '-')) {
                idx++
            }
            val numStr = src.substring(start, idx)
            return if (numStr.contains('.') || numStr.contains('e') || numStr.contains('E')) {
                numStr.toDoubleOrNull() ?: 0.0
            } else {
                numStr.toLongOrNull() ?: 0L
            }
        }

        private fun parseBoolean(): Boolean {
            return if (src.startsWith("true", idx)) {
                idx += 4
                true
            } else if (src.startsWith("false", idx)) {
                idx += 5
                false
            } else {
                idx++
                false
            }
        }

        private fun parseNull(): Any? {
            if (src.startsWith("null", idx)) {
                idx += 4
            }
            return null
        }

        private fun skipWhitespace() {
            while (idx < src.length && src[idx].isWhitespace()) {
                idx++
            }
        }
    }
}
