package com.ivangames.blockpop

class Piece(
    val shape: Array<IntArray>,
    val color: Int
) {
    val width: Int get() = shape[0].size
    val height: Int get() = shape.size

    companion object {
        // Стандартные фигуры Block Blast
        private val SHAPES = listOf(
            // 1x1
            arrayOf(intArrayOf(1)),
            // 1x2
            arrayOf(intArrayOf(1, 1)),
            arrayOf(intArrayOf(1), intArrayOf(1)),
            // 1x3
            arrayOf(intArrayOf(1, 1, 1)),
            arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1)),
            // 1x4
            arrayOf(intArrayOf(1, 1, 1, 1)),
            arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
            // 1x5
            arrayOf(intArrayOf(1, 1, 1, 1, 1)),
            arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
            // 2x2 квадрат
            arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
            // 3x3 квадрат
            arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 1, 1), intArrayOf(1, 1, 1)),
            // L-образные
            arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1)),
            arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 0)),
            arrayOf(intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1)),
            arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)),
            // J-образные
            arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1)),
            arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),
            arrayOf(intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0)),
            arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 0, 1)),
            // T-образные
            arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)),
            arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),
            arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 0)),
            arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(0, 1)),
            // S/Z-образные
            arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
            arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
            arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1)),
            arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0))
        )

        private val COLORS = intArrayOf(
            0xFF6A1B9A.toInt(), // фиолетовый
            0xFF2196F3.toInt(), // синий
            0xFF00BCD4.toInt(), // голубой
            0xFFE91E63.toInt(), // розовый
            0xFFFF9800.toInt(), // оранжевый
            0xFF4CAF50.toInt(), // зелёный
            0xFFE53935.toInt(), // красный
            0xFFFFEB3B.toInt()  // жёлтый
        )

        fun random(): Piece {
            val shape = SHAPES.random()
            val color = COLORS.random()
            return Piece(shape, color)
        }
    }
}
