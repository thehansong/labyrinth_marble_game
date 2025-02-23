package com.example.labyrinthmarblegame

import android.content.Context;
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

// Data classes for game entities
data class Vector2(var x: Float, var y: Float);
data class Vector3(var r: Float, var g: Float, var b: Float);
data class GameEntity(
    var name: String,
    var activeState: Boolean,
    var collisionLayer: Int,
    var velocity: Vector2,
    var position: Vector2,
    var scale: Vector2,
    var rotation: Float,
    var color: Vector3,
    var texture: Bitmap? = null // Reference to texture (mipmap)
);

class MarbleGameLogic(private val context: Context) : SensorEventListener {
    val entities = mutableListOf<GameEntity>();
    private var playerVelocity = Vector2(0f, 0f);
    private var lastFrameTime = System.nanoTime();
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
    private val acceleration = Vector2(0f, 0f)
    private val friction = 0.98f // Slows velocity over time (probably don't need this tbh)
    private val gravityFactor = 5f // Adjust as needed for responsiveness

    // Boundary variables
    private val yMin = -10f  // Top
    private val yMax = 8f    // Bottom
    private val xMin = -5f
    private val xMax = 5f
    private val wallThickness = 1f

    // Level dimensions calculated from boundaries
    private val levelWidth = xMax - xMin
    private val levelHeight = yMax - yMin
    private val levelCenterY = (yMax + yMin) / 2

    init {
        initializeEntities();
        registerSensors();
    }

    fun createButtons(context: Context): List<Button> {
        val buttons = mutableListOf<Button>()

        val controls = listOf(
            "Up" to Vector2(0f, -gravityFactor),
            "Down" to Vector2(0f, gravityFactor),
            "Left" to Vector2(-gravityFactor, 0f),
            "Right" to Vector2(gravityFactor, 0f)
        )

        controls.forEachIndexed { index, (label, force) ->
            buttons.add(Button(context).apply {
                text = label
                setBackgroundColor(Color.LTGRAY)
                alpha = 0.8f
                layoutParams = FrameLayout.LayoutParams(
                    200, 200
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    bottomMargin = if (label == "Up") 350 else 150
                    leftMargin = if (label == "Left") -250 else if (label == "Right") 250 else 0
                }
                setOnClickListener {
                    acceleration.x += force.x
                    acceleration.y += force.y
                }
            })
        }

        return buttons
    }

    private fun initializeEntities() {
        // Add these in draw order, later ones are drawn on top
        val wallPositions = listOf(
            Vector2(0f, yMin),         // Top wall
            Vector2(0f, yMax),          // Bottom wall
            Vector2(xMin, levelCenterY),   // Left wall
            Vector2(xMax, levelCenterY)    // Right wall
        )
        val wallScales = listOf(
            Vector2(levelWidth, wallThickness),  // Top (full width)
            Vector2(levelWidth, wallThickness),  // Bottom (full width)
            Vector2(wallThickness, levelHeight), // Left
            Vector2(wallThickness, levelHeight)  // Right
        )

        // Add floor
        entities.add(GameEntity(
            name = "Floor",
            activeState = true,
            collisionLayer = 2,
            velocity = Vector2(0f, 0f),
            position = Vector2(0f, levelCenterY),
            scale = Vector2(levelWidth, levelHeight),
            rotation = 0f,
            color = Vector3(0.5f, 0.5f, 0.5f)
            // texture remains null so a solid color is drawn
        ))

        // Add walls
        wallPositions.forEachIndexed { index, pos ->
            entities.add(GameEntity(
                name = "Wall$index",
                activeState = true,
                collisionLayer = 2,
                velocity = Vector2(0f, 0f),
                position = pos,
                scale = wallScales[index],
                rotation = 0f,
                color = Vector3(1f, 1f, 1f)
                // texture remains null so a solid color is drawn
            ))
        }

        // Add player (marble) with the bitmap assigned
        entities.add(GameEntity(
            name = "Player",
            activeState = true,
            collisionLayer = 1,
            velocity = Vector2(0f, 0f),
            position = Vector2(0f, 0f),
            scale = Vector2(1f, 1f),
            rotation = 0f,
            color = Vector3(1f, 1f, 1f),
            texture = BitmapFactory.decodeResource(context.resources, R.drawable.ball_blue_small)
        ))
    }

    private fun registerSensors() {
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    fun update() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        entities.find { it.name == "Player" }?.let { player ->
            // Apply acceleration to velocity
            playerVelocity.x += acceleration.x * gravityFactor * deltaTime
            playerVelocity.y += acceleration.y * gravityFactor * deltaTime

            // Apply friction (kinda buggy)
            playerVelocity.x *= friction
            playerVelocity.y *= friction

            // Update position
            player.position.x += playerVelocity.x * deltaTime
            player.position.y += playerVelocity.y * deltaTime

            // Collision detection with walls (hardcoded)
            player.position.x = player.position.x.coerceIn(xMin + wallThickness, xMax - wallThickness)
            if (player.position.x == xMin + wallThickness || player.position.x == xMax - wallThickness) {
                playerVelocity.x = 0f
                acceleration.x = 0f
            }

            player.position.y = player.position.y.coerceIn(yMin + wallThickness, yMax - wallThickness)
            if (player.position.y == yMin + wallThickness || player.position.y == yMax - wallThickness) {
                playerVelocity.y = 0f
                acceleration.y = 0f
            }

            //println("Acceleration: (${acceleration.x}, ${acceleration.y})")
            //println("Velocity: (${playerVelocity.x}, ${playerVelocity.y})")
            //println("Position: (${player.position.x}, ${player.position.y})")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            playerVelocity.x = event.values[0] * -1;
            playerVelocity.y = event.values[1];
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}