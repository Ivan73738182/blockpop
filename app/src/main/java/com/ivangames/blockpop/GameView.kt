package com.ivangames.blockpop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        const val BOARD_SIZE = 8
    }

    // Поле 8x8, 0 = пусто, иначе — индекс цвета
    private val board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { 0 } }

    // 3 фигуры внизу
    private val pieces = mutableListOf<Piece?>(null, null, null)

    // Текущая перетаскиваемая фигура
    private var draggingPiece: Piece? = null
    private var draggingIndex: Int = -1
    private var dragX = 0f
    private var dragY = 0f
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    // Счёт
    var score: Int = 0
        private set

    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E1E2E")
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A2A3A")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var cellSize = 0f
    private var boardLeft = 0f
    private var boardTop = 0f
    private var piecesTop = 0f
    private var pieceCellSize = 0f

    init {
        generatePieces()
    }

    private fun generatePieces() {
        for (i in 0..2) {
            if (pieces.getOrNull(i) == null) {
                pieces[i] = Piece.random()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Поле: 80% ширины
        val boardWidth = w * 0.9f
        cellSize = boardWidth / BOARD_SIZE
        boardLeft = (w - boardWidth) / 2f
        boardTop = h * 0.15f

        // Фигуры внизу: 3 штуки
        pieceCellSize = cellSize * 0.7f
        piecesTop = boardTop + boardWidth + cellSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем поле
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val left = boardLeft + c * cellSize
                val top = boardTop + r * cellSize
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                if (board[r][c] == 0) {
                    canvas.drawRect(rect, emptyPaint)
                } else {
                    cellPaint.color = board[r][c]
                    canvas.drawRect(rect, cellPaint)
                }
                canvas.drawRect(rect, borderPaint)
            }
        }

        // Рисуем 3 фигуры внизу
        val pieceWidth = width / 3f
        for (i in 0..2) {
            val piece = pieces.getOrNull(i) ?: continue
            if (draggingIndex == i) continue // перетаскиваемая не рисуется на месте

            val startX = i * pieceWidth + pieceWidth / 2f
            val startY = piecesTop + pieceCellSize * 2

            drawPiece(canvas, piece, startX, startY)
        }

        // Перетаскиваемая фигура
        draggingPiece?.let {
            drawPiece(canvas, it, dragX - dragOffsetX, dragY - dragOffsetY)
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Piece, centerX: Float, centerY: Float) {
        val w = piece.width * pieceCellSize
        val h = piece.height * pieceCellSize
        val startX = centerX - w / 2f
        val startY = centerY - h / 2f

        cellPaint.color = piece.color

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1) {
                    val left = startX + c * pieceCellSize
                    val top = startY + r * pieceCellSize
                    val rect = RectF(left, top, left + pieceCellSize, top + pieceCellSize)
                    canvas.drawRect(rect, cellPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val pieceWidth = width / 3f
                val idx = (event.x / pieceWidth).toInt()
                if (idx in 0..2 && event.y > piecesTop) {
                    val piece = pieces.getOrNull(idx) ?: return false
                    draggingIndex = idx
                    draggingPiece = piece
                    dragX = event.x
                    dragY = event.y

                    // Смещение от центра фигуры
                    val pieceCenterX = idx * pieceWidth + pieceWidth / 2f
                    val pieceCenterY = piecesTop + pieceCellSize * 2
                    dragOffsetX = event.x - pieceCenterX
                    dragOffsetY = event.y - pieceCenterY

                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggingPiece != null) {
                    dragX = event.x
                    dragY = event.y
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (draggingPiece != null) {
                    tryPlacePiece()
                    draggingPiece = null
                    draggingIndex = -1
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun tryPlacePiece() {
        val piece = draggingPiece ?: return

        // Координаты левого-верхнего угла фигуры
        val left = dragX - dragOffsetX - piece.width * pieceCellSize / 2f
        val top = dragY - dragOffsetY - piece.height * pieceCellSize / 2f

        // Позиция на поле
        val col = ((left - boardLeft) / cellSize).roundToInt()
        val row = ((top - boardTop) / cellSize).roundToInt()

        // Проверка границ
        if (row < 0 || col < 0 ||
            row + piece.height > BOARD_SIZE ||
            col + piece.width > BOARD_SIZE) {
            return
        }

        // Проверка, что все клетки пустые
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1 && board[row + r][col + c] != 0) {
                    return
                }
            }
        }

        // Ставим фигуру
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1) {
                    board[row + r][col + c] = piece.color
                }
            }
        }

        // Очки
        score += countCells(piece) * 2

        // Сброс фигуры на месте
        pieces[draggingIndex] = null

        // Проверка рядов
        clearLines()

        // Генерируем новые, если все израсходованы
        if (pieces.all { it == null }) {
            generatePieces()
        }

        // Проверка на конец игры
        if (!canPlaceAny()) {
            onGameOver?.invoke()
        }

        onScoreChanged?.invoke(score)
        invalidate()
    }

    private fun countCells(piece: Piece): Int {
        var count = 0
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1) count++
            }
        }
        return count
    }

    private fun clearLines() {
        val toClear = mutableSetOf<Pair<Int, Int>>()

        // Строки
        for (r in 0 until BOARD_SIZE) {
            if (board[r].all { it != 0 }) {
                for (c in 0 until BOARD_SIZE) toClear.add(r to c)
            }
        }

        // Столбцы
        for (c in 0 until BOARD_SIZE) {
            if ((0 until BOARD_SIZE).all { board[it][c] != 0 }) {
                for (r in 0 until BOARD_SIZE) toClear.add(r to c)
            }
        }

        // Очистка + очки
        if (toClear.isNotEmpty()) {
            for ((r, c) in toClear) board[r][c] = 0
            score += toClear.size * 10
        }
    }

    private fun canPlaceAny(): Boolean {
        for (piece in pieces) {
            if (piece == null) continue
            for (r in 0..BOARD_SIZE - piece.height) {
                for (c in 0..BOARD_SIZE - piece.width) {
                    if (canFit(piece, r, c)) return true
                }
            }
        }
        return false
    }

    private fun canFit(piece: Piece, row: Int, col: Int): Boolean {
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1 && board[row + r][col + c] != 0) {
                    return false
                }
            }
        }
        return true
    }

    fun reset() {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                board[r][c] = 0
            }
        }
        score = 0
        pieces[0] = null
        pieces[1] = null
        pieces[2] = null
        generatePieces()
        onScoreChanged?.invoke(score)
        invalidate()
    }

    private fun Float.roundToInt(): Int = kotlin.math.round(this).toInt()
}
