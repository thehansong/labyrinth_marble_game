package com.example.labyrinthmarblegame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.view.View
import android.view.Gravity

// Main game view that contains both the canvas and buttons
class MarbleGameView(context: Context, viewModel: MarbleGameViewModel) : FrameLayout(context) {
    private val gameCanvas: GameCanvas
    private val gameLogic: MarbleGameLogic = MarbleGameLogic(context, viewModel)
    // This gameLogic is needed, not sure why I can't pass this as an argument to GameView

    private var navCallback: (() -> Unit)? = null

    init {
        gameCanvas = GameCanvas(context, gameLogic)
        addView(gameCanvas)

        // Set up the listener
        gameLogic.setGameEventListener(object : MarbleGameLogic.GameEventListener {
            override fun onGameCompleted() {
                // Call the nav callback on the UI thread
                post {
                    navCallback?.invoke()
                }
            }
        })

        // Add buttons from game logic
        //gameLogic.createButtons(context).forEach { button ->
        //    addView(button)
        //}
    }

    fun setNavigationCallback(callback: () -> Unit) {
        navCallback = callback
    }
}

// The actual canvas where we draw the game
private class GameCanvas(context: Context, private val gameLogic: MarbleGameLogic) : View(context) {
    private val paint = Paint()
    private val drawRect = RectF()

    // World space constants
    private val WORLD_WIDTH = 10f
    private val WORLD_HEIGHT = 10f
    private var pixelsPerUnit = 0f

    init {
        // Nothing to do
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pixelsPerUnit = minOf(
            width.toFloat() / WORLD_WIDTH,
            height.toFloat() / WORLD_HEIGHT
        )
        //gameLogic.setScreenDimensions(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        val worldOffsetX = (width - WORLD_WIDTH * pixelsPerUnit) / 2
        val worldOffsetY = (height - WORLD_HEIGHT * pixelsPerUnit) / 2

        gameLogic.entities.forEach { entity ->
            if (entity.activeState) {
                drawEntity(canvas, entity, worldOffsetX, worldOffsetY)
            }
        }

        gameLogic.update()
        invalidate()
    }

    private fun drawEntity(canvas: Canvas, entity: GameEntity, offsetX: Float, offsetY: Float) {
        val screenX = offsetX + (entity.position.x + WORLD_WIDTH / 2) * pixelsPerUnit
        val screenY = offsetY + (entity.position.y + WORLD_HEIGHT / 2) * pixelsPerUnit
        val screenWidth = entity.scale.x * pixelsPerUnit
        val screenHeight = entity.scale.y * pixelsPerUnit

        drawRect.set(
            screenX - screenWidth / 2,
            screenY - screenHeight / 2,
            screenX + screenWidth / 2,
            screenY + screenHeight / 2
        )

        if (entity.texture == null) {
            // Draw solid color
            paint.reset()
            paint.color = Color.rgb(
                (entity.color.r * 255).toInt(),
                (entity.color.g * 255).toInt(),
                (entity.color.b * 255).toInt()
            )
            paint.style = Paint.Style.FILL
            canvas.drawRect(drawRect, paint)
        } else {
            // Use BitmapShader for tiling
            entity.texture?.let { bitmap ->
                if (entity.isTiled) {
                    val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    paint.shader = shader

                    // Scale the texture according to tilesX and tilesY
                    val matrix = Matrix()
                    matrix.setScale(
                        entity.tilesX / entity.scale.x,
                        entity.tilesY / entity.scale.y
                    )
                    shader.setLocalMatrix(matrix)

                    canvas.drawRect(drawRect, paint)
                } else {
                    // Regular texture rendering
                    paint.shader = null
                    canvas.drawBitmap(bitmap, null, drawRect, paint)
                }
            }
        }
    }
}

