package com.droid.whiteboard.data

import com.droid.whiteboard.model.Whiteboard
import com.droid.whiteboard.model.WhiteboardMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

interface WhiteboardRepository {
    suspend fun getAllWhiteboards(): List<WhiteboardMetadata>
    suspend fun getWhiteboard(id: String): Whiteboard?
    suspend fun saveWhiteboard(whiteboard: Whiteboard): Boolean
    suspend fun deleteWhiteboard(id: String): Boolean
    suspend fun createNewWhiteboard(title: String = "", isDarkMode: Boolean = true): Whiteboard
}

class FileWhiteboardRepository(
    private val storageDir: File
) : WhiteboardRepository {

    private val mutex = Mutex()
    private val indexFile = File(storageDir, "index.json")

    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    override suspend fun getAllWhiteboards(): List<WhiteboardMetadata> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val list = readIndex()
            if (list.isEmpty()) {
                val defaultBoard = createDefaultBoardInternal()
                listOf(
                    WhiteboardMetadata(
                        id = defaultBoard.id,
                        title = defaultBoard.title,
                        createdAt = defaultBoard.createdAt,
                        updatedAt = defaultBoard.updatedAt,
                        isDarkMode = defaultBoard.isDarkMode,
                        strokeCount = 0
                    )
                )
            } else {
                list
            }
        }
    }

    override suspend fun getWhiteboard(id: String): Whiteboard? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val boardFile = File(storageDir, "board_$id.json")
            if (!boardFile.exists()) {
                val index = readIndex()
                if (index.isEmpty()) {
                    createDefaultBoardInternal()
                    if (boardFile.exists()) {
                        readBoardFromFile(boardFile)
                    } else null
                } else null
            } else {
                readBoardFromFile(boardFile)
            }
        }
    }

    override suspend fun saveWhiteboard(whiteboard: Whiteboard): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val boardFile = File(storageDir, "board_${whiteboard.id}.json")
                val json = WhiteboardJson.whiteboardToJson(whiteboard)
                boardFile.writeText(json)

                // Update index
                val index = readIndex().toMutableList()
                val meta = WhiteboardMetadata(
                    id = whiteboard.id,
                    title = whiteboard.title,
                    createdAt = whiteboard.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    isDarkMode = whiteboard.isDarkMode,
                    strokeCount = whiteboard.strokes.size
                )
                val existingIndex = index.indexOfFirst { it.id == whiteboard.id }
                if (existingIndex >= 0) {
                    index[existingIndex] = meta
                } else {
                    index.add(0, meta)
                }
                writeIndex(index)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun deleteWhiteboard(id: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val boardFile = File(storageDir, "board_$id.json")
                if (boardFile.exists()) {
                    boardFile.delete()
                }
                val index = readIndex().filter { it.id != id }
                writeIndex(index)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun createNewWhiteboard(title: String, isDarkMode: Boolean): Whiteboard = withContext(Dispatchers.IO) {
        mutex.withLock {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val boardTitle = title.ifBlank { "Whiteboard ${readIndex().size + 1}" }
            val board = Whiteboard(
                id = id,
                title = boardTitle,
                createdAt = now,
                updatedAt = now,
                isDarkMode = isDarkMode,
                strokes = emptyList()
            )

            val boardFile = File(storageDir, "board_$id.json")
            boardFile.writeText(WhiteboardJson.whiteboardToJson(board))

            val index = readIndex().toMutableList()
            index.add(
                0,
                WhiteboardMetadata(
                    id = board.id,
                    title = board.title,
                    createdAt = board.createdAt,
                    updatedAt = board.updatedAt,
                    isDarkMode = board.isDarkMode,
                    strokeCount = 0
                )
            )
            writeIndex(index)
            board
        }
    }

    private fun createDefaultBoardInternal(): Whiteboard {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val board = Whiteboard(
            id = id,
            title = "My Whiteboard",
            createdAt = now,
            updatedAt = now,
            isDarkMode = true,
            strokes = emptyList()
        )
        val boardFile = File(storageDir, "board_$id.json")
        boardFile.writeText(WhiteboardJson.whiteboardToJson(board))
        writeIndex(
            listOf(
                WhiteboardMetadata(
                    id = id,
                    title = board.title,
                    createdAt = now,
                    updatedAt = now,
                    isDarkMode = true,
                    strokeCount = 0
                )
            )
        )
        return board
    }

    private fun readIndex(): List<WhiteboardMetadata> {
        if (!indexFile.exists()) return emptyList()
        return try {
            WhiteboardJson.parseIndex(indexFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeIndex(list: List<WhiteboardMetadata>) {
        indexFile.writeText(WhiteboardJson.indexToJson(list))
    }

    private fun readBoardFromFile(file: File): Whiteboard? {
        return try {
            WhiteboardJson.parseWhiteboard(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
