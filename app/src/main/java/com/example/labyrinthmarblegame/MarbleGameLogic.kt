package com.example.labyrinthmarblegame

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.FrameLayout
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Data classes for game entities
data class Vector2(var x: Float, var y: Float)
data class Vector3(var r: Float, var g: Float, var b: Float)
data class GameEntity(
    var name: String,
    var activeState: Boolean,
    var collisionLayer: Int,
    var velocity: Vector2,
    var position: Vector2,
    var scale: Vector2,
    var rotation: Float,
    var color: Vector3,
    var texture: Bitmap? = null,
    //var isTiled: Boolean,
    //var tilesX: Int,      // How many times to repeat texture horizontally
    //var tilesY: Int       // How many times to repeat texture vertically
)

class MarbleGameLogic(context: Context, private val viewModel: MarbleGameViewModel) : SensorEventListener {
    val entities = mutableListOf<GameEntity>()
    private var gameLevels = MarbleGameLevels(context)

    private var lastFrameTime = System.nanoTime()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val collisionSystem = CollisionSystem()

    private var currentLevel = gameLevels.Level1  // Start with level 1
    private var currentLevelNumber = 1

    private var playerVelocity = Vector2(0f, 0f)
    private val acceleration = Vector2(0f, 0f)
    private val friction = 0.98f // Slows velocity over time (probably don't need this tbh)
    private val gravityFactor = 5f // Adjust as needed for responsiveness
    private val playerStartingPosition = Vector2(0f, 0f)
    private var playerStartingScale = Vector2(0.5f, 0.5f)

    private var isPlayerDead = false
    private var isPlayerReviving = false
    private var animationTimer = 0f
    private val animationDuration = 0.5f
    private var changingLevels = false

    // Highscore variables
    private var gameStartTime: Long = 0
    private var gameCompletionTime: Long = 0
    private var isTimerRunning = false

    init {
        initializeEntities()
        registerSensors()
        startTimer()
    }

    // Listener for navigating back to main menu/highscores
    interface GameEventListener {
        fun onGameCompleted()
    }

    private var gameEventListener: GameEventListener? = null

    fun setGameEventListener(listener: GameEventListener) {
        gameEventListener = listener
    }

    // This is for debug before we had accelerometer working
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
        // Clear existing entities
        entities.clear()

        // Create entities from level data
        entities.addAll(gameLevels.createEntities(currentLevel))

        // Store starting position for hole collisions
        playerStartingPosition.x = currentLevel.playerStart.x
        playerStartingPosition.y = currentLevel.playerStart.y

        changingLevels = false
    }

    private fun registerSensors() {
        // Use accelerometer instead of gyroscope
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun update() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        val player = entities.find { it.name == "Player" } ?: return

        if (isPlayerDead) {
            // Shrink player while they're falling into a hole
            animationTimer += deltaTime
            player.scale.x = playerStartingScale.x * (1f - (animationTimer / animationDuration))
            player.scale.y = playerStartingScale.y * (1f - (animationTimer / animationDuration))
            if (animationTimer >= animationDuration) {
                // Finished shrinking
                isPlayerDead = false
                animationTimer = 0f
                player.scale.x = 0f
                player.scale.y = 0f

                // Reset player position and velocities
                player.position.x = playerStartingPosition.x
                player.position.y = playerStartingPosition.y
                playerVelocity.x = 0f
                playerVelocity.y = 0f
                acceleration.x = 0f
                acceleration.y = 0f

                // Start reviving (growing back)
                isPlayerReviving = true
            }
        } else if (isPlayerReviving) {
            // Enlarge player while they're reviving
            animationTimer += deltaTime
            player.scale.x = (animationTimer / animationDuration) * playerStartingScale.x
            player.scale.y = (animationTimer / animationDuration) * playerStartingScale.y
            if (animationTimer >= animationDuration) {
                // Finished reviving
                isPlayerReviving = false
                animationTimer = 0f
                player.scale.x = playerStartingScale.x
                player.scale.y = playerStartingScale.y
            }
        } else {
            // Regular game logic (movement, collision, etc.)
            // Store previous position for collision resolution
            val previousX = player.position.x
            val previousY = player.position.y

            // Apply acceleration to velocity
            playerVelocity.x += acceleration.x * gravityFactor * deltaTime
            playerVelocity.y += acceleration.y * gravityFactor * deltaTime

            // Apply friction
            playerVelocity.x *= friction
            playerVelocity.y *= friction

            // Update position
            player.position.x += playerVelocity.x * deltaTime
            player.position.y += playerVelocity.y * deltaTime

            // Collision handling
            val playerCircle = player.toCircle()
            entities.filter { it != player }.forEach { entity ->
                when (entity.collisionLayer) {
                    0 -> { // Collision with walls
                        val wallRect = entity.toRectangle()
                        collisionSystem.checkCircleRectangleCollision(playerCircle, wallRect)?.let { (collisionPoint, normal) ->
                            player.position.x = previousX
                            player.position.y = previousY

                            val impactSpeed = kotlin.math.sqrt(playerVelocity.x * playerVelocity.x + playerVelocity.y * playerVelocity.y)
                            if (impactSpeed > 10f) {
                                soundPool.play(wallCollisionSoundId, 1f, 1f, 0, 0, 1f)
                            }

                            if (kotlin.math.abs(normal.x) > 0.1f) {
                                playerVelocity.x *= -0.5f
                                acceleration.x = 0f
                            }
                            if (kotlin.math.abs(normal.y) > 0.1f) {
                                playerVelocity.y *= -0.5f
                                acceleration.y = 0f
                            }
                        }
                    }
                    1 -> { // Hole
                        val otherCircle = entity.toCircle()
                        otherCircle.radius -= 0.2f
                        if (collisionSystem.checkCircleCircleCollision(playerCircle, otherCircle) && !isPlayerDead && !isPlayerReviving) {
                            // Start shrinking animation when player touches a hole
                            playerStartingScale.x = player.scale.x
                            playerStartingScale.y = player.scale.y
                            isPlayerDead = true
                            animationTimer = 0f
                            soundPool.play(gameRestartSoundId, 1f, 1f, 0, 0, 1f)
                        }
                    }
                    2 -> { // Goal
                        val otherCircle = entity.toCircle()
                        if (collisionSystem.checkCircleCircleCollision(playerCircle, otherCircle) && !changingLevels) {
                            changingLevels = true
                            loadNextLevel()
                        }
                    }
                }
            }
        }
    }

    // Method to load next level
    private fun loadNextLevel() {
        currentLevelNumber++
        // Select the next level based on number
        currentLevel = when (currentLevelNumber) {
            1 -> gameLevels.Level1
            2 -> gameLevels.Level2
            // Add more levels here as needed
            else -> {
                // Game completed, out of levels
                soundPool.play(gameClearedSoundId, 1f, 1f, 0, 0, 1f)

                // Stop timer when game is completed
                stopTimer()

                // Save score to database
                // Use lifecycleScope to launch the coroutine
                viewModel.viewModelScope.launch {
                    val score = MarbleGameScore(
                        level = currentLevelNumber - 1, // The level player just completed
                        completionTime = gameCompletionTime,
                        playerName = viewModel.playerName.value
                    )
                    viewModel.insertScore(score)
                }

                gameEventListener?.onGameCompleted() // Return from this android view
                return
            }
        }
        soundPool.play(nextLevelSoundId, 1f, 1f, 0, 0, 1f)
        // Reset player and reload entities
        initializeEntities()
        // Reset player velocity
        playerVelocity = Vector2(0f, 0f)
        acceleration.x = 0f
        acceleration.y = 0f
    }

    // Start the game timer
    private fun startTimer() {
        gameStartTime = System.currentTimeMillis()
        isTimerRunning = true
    }

    // Stop the timer and calculate the completion time
    private fun stopTimer() {
        if (isTimerRunning) {
            val endTime = System.currentTimeMillis()
            gameCompletionTime = (endTime - gameStartTime) / 1000 // Convert to seconds
            isTimerRunning = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Use accelerometer sensor values to control marble tilt
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Adjust the axes as needed for your game's control scheme
            acceleration.x = -event.values[0]
            acceleration.y = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
