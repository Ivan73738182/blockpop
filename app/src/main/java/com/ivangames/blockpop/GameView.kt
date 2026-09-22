package com.ivangames.blockpop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        const val BOARD_SIZE = 8
        const val CELL_GAP = 4f // отступ между кубиками
    }

    private val board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { 0 } }
    private val pieces = arrayOfNulls<Piece>(3)

    private var draggingPiece: Piece? = null
    private var draggingIndex: Int = -1
    private var dragX = 0f
    private var dragY = 0f
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    var score: Int = 0
        private set

    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null

    // Краски
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#14141F")
    }
    private val emptyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A2A3A")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Размеры
    private var cellSize = 0f
    private var boardLeft = 0f
    private var boardTop = 0f
    private var piecesTop = 0f

    init {
        generatePieces()
    }

    private fun generatePieces() {
        for (i in 0..2) {
            if (pieces[i] == null) {
                pieces[i] = Piece.random()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val boardWidth = w * 0.92f
        cellSize = boardWidth / BOARD_SIZE
        boardLeft = (w - boardWidth) / 2f
        boardTop = h * 0.16f

        piecesTop = boardTop + boardWidth + cellSize * 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // ===== Поле =====
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val left = boardLeft + c * cellSize + CELL_GAP / 2
                val top = boardTop + r * cellSize + CELL_GAP / 2
                val right = boardLeft + (c + 1) * cellSize - CELL_GAP / 2
                val bottom = boardTop + (r + 1) * cellSize - CELL_GAP / 2
                val rect = RectF(left, top, right, bottom)
                val radius = cellSize * 0.18f

                if (board[r][c] == 0) {
                    canvas.drawRoundRect(rect, radius, radius, emptyPaint)
                    canvas.drawRoundRect(rect, radius, radius, emptyBorderPaint)
                } else {
                    cellPaint.color = board[r][c]
                    canvas.drawRoundRect(rect, radius, radius, cellPaint)
                }
            }
        }

        // ===== 3 фигуры внизу =====
        val slotWidth = width / 3f
        for (i in 0..2) {
            val piece = pieces[i] ?: continue
            if (draggingIndex == i) continue

            val centerX = i * slotWidth + slotWidth / 2f
            val centerY = piecesTop + cellSize * 2f

            drawPiece(canvas, piece, centerX, centerY, cellSize)
        }

        // ===== Перетаскиваемая фигура =====
        draggingPiece?.let {
            drawPiece(canvas, it, dragX - dragOffsetX, dragY - dragOffsetY, cellSize)
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Piece, centerX: Float, centerY: Float, size: Float) {
        val w = piece.width * size
        val h = piece.height * size
        val startX = centerX - w / 2f
        val startY = centerY - h / 2f

        cellPaint.color = piece.color

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1) {
                    val left = startX + c * size + CELL_GAP / 2
                    val top = startY + r * size + CELL_GAP / 2
                    val right = startX + (c + 1) * size - CELL_GAP / 2
                    val bottom = startY + (r + 1) * size - CELL_GAP / 2
                    val rect = RectF(left, top, right, bottom)
                    val radius = size * 0.18f
                    canvas.drawRoundRect(rect, radius, radius, cellPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val slotWidth = width / 3f
                val idx = (event.x / slotWidth).toInt().coerceIn(0, 2)
                if (event.y > piecesTop - cellSize && pieces[idx] != null) {
                    val piece = pieces[idx] ?: return false

                    // Смещение от центра фигуры
                    val pieceCenterX = idx * slotWidth + slotWidth / 2f
                    val pieceCenterY = piecesTop + cellSize * 2f

                    dragOffsetX = event.x - pieceCenterX
                    dragOffsetY = event.y - pieceCenterY

                    draggingPiece = piece
                    draggingIndex = idx
                    dragX = event.x
                    dragY = event.y
                    invalidate()
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

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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

        // Центр фигуры при отпускании
        val centerX = dragX - dragOffsetX
        val centerY = dragY - dragOffsetY

        // Левый-верхний угол фигуры (по центру)
        val left = centerX - piece.width * cellSize / 2f
        val top = centerY - piece.height * cellSize / 2f

        // В клетках поля
        val col = ((left - boardLeft) / cellSize).roundToInt()
        val row = ((top - boardTop) / cellSize).roundToInt()

        if (row < 0 || col < 0 ||
            row + piece.height > BOARD_SIZE ||
            col + piece.width > BOARD_SIZE
        ) return

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1 && board[row + r][col + c] != 0) return
            }
        }

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c] == 1) {
                    board[row + r][col + c] = piece.color
                }
            }
        }

        score += countCells(piece) * 2
        pieces[draggingIndex] = null

        clearLines()

        if (pieces.all { it == null }) generatePieces()

        if (!canPlaceAny()) onGameOver?.invoke()

        onScoreChanged?.invoke(score)
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

        for (r in 0 until BOARD_SIZE) {
            if (board[r].all { it != 0 }) {
                for (c in 0 until BOARD_SIZE) toClear.add(r to c)
            }
        }
        for (c in 0 until BOARD_SIZE) {
            if ((0 until BOARD_SIZE).all { board[it][c] != 0 }) {
                for (r in 0 until BOARD_SIZE) toClear.add(r to c)
            }
        }

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
}
