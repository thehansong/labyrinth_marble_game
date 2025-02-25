package com.example.labyrinthmarblegame

import android.content.Context
import android.graphics.BitmapFactory

data class LevelWall(
    val start: Vector2,
    val end: Vector2
)

data class LevelHole(
    val position: Vector2
)

data class LevelGoal(
    val position: Vector2
)

data class LevelData(
    val playerStart: Vector2,
    val walls: List<LevelWall>,
    val holes: List<LevelHole> = emptyList(),
    val goals: List<LevelGoal> = emptyList()
)

class MarbleGameLevels(private val context: Context) {
    private val wallThickness = 0.5f

    // Boundary variables
    private val yMin = -10f  // Top
    private val yMax = 8f    // Bottom
    private val xMin = -5f
    private val xMax = 5f

    // Level dimensions calculated from boundaries
    private val levelWidth = xMax - xMin
    private val levelHeight = yMax - yMin
    private val levelCenterX = (xMax + xMin) / 2
    private val levelCenterY = (yMax + yMin) / 2

    // Helper function to create a wall entity from start/end points
    private fun createWallEntity(wall: LevelWall): GameEntity {
        val dx = wall.end.x - wall.start.x
        val dy = wall.end.y - wall.start.y
        val length = kotlin.math.sqrt(dx * dx + dy * dy)
        val angle = kotlin.math.atan2(dy, dx)

        return GameEntity(
            name = "Wall",
            activeState = true,
            collisionLayer = 0,
            velocity = Vector2(0f, 0f),
            position = Vector2(
                (wall.start.x + wall.end.x) / 2,
                (wall.start.y + wall.end.y) / 2
            ),
            // Switch x and y based on the wall's orientation
            scale = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                // Horizontal wall: add wallThickness to x-axis
                Vector2(length + wallThickness, wallThickness)
            } else {
                // Vertical wall: add wallThickness to y-axis
                Vector2(wallThickness, length + wallThickness)
            },
            rotation = angle,
            color = Vector3(0.3f, 1f, 0.3f),
            //texture = BitmapFactory.decodeResource(context.resources, R.drawable.block_square),
        )
    }

    // Helper function to create game entities from level data
    fun createEntities(levelData: LevelData): List<GameEntity> {
        val entities = mutableListOf<GameEntity>()

        // Create floor
        entities.add(GameEntity(
            name = "Floor",
            activeState = true,
            collisionLayer = -1,
            velocity = Vector2(0f, 0f),
            position = Vector2(levelCenterX, levelCenterY),
            scale = Vector2(levelWidth, levelHeight),
            rotation = 0f,
            color = Vector3(0.5f, 0.5f, 0.5f),
            BitmapFactory.decodeResource(context.resources, R.drawable.background_brown),
            true,
            levelWidth.toInt(),
            levelHeight.toInt()
        ))

        // Create walls
        levelData.walls.forEach { wall ->
            entities.add(createWallEntity(wall))
        }

        // Create holes
        levelData.holes.forEach { hole ->
            entities.add(GameEntity(
                name = "Hole",
                activeState = true,
                collisionLayer = 1,
                velocity = Vector2(0f, 0f),
                position = hole.position,
                scale = Vector2(0.8f, 0.8f),  // Adjust hole size as needed
                rotation = 0f,
                color = Vector3(1f, 1f, 1f),
                BitmapFactory.decodeResource(context.resources, R.drawable.hole)
            ))
        }

        // Create goals
        levelData.goals.forEach { goal ->
            entities.add(GameEntity(
                name = "Goal",
                activeState = true,
                collisionLayer = 2,
                velocity = Vector2(0f, 0f),
                position = goal.position,
                scale = Vector2(1f, 1f),  // Adjust goal size as needed
                rotation = 0f,
                color = Vector3(1f, 1f, 1f),
                BitmapFactory.decodeResource(context.resources, R.drawable.hole_small_end)
            ))
        }

        // Create player
        entities.add(GameEntity(
            name = "Player",
            activeState = true,
            collisionLayer = 0,
            velocity = Vector2(0f, 0f),
            position = levelData.playerStart,
            scale = Vector2(0.5f, 0.5f),
            rotation = 0f,
            color = Vector3(1f, 1f, 1f),
            BitmapFactory.decodeResource(context.resources, R.drawable.ball_blue_small)
        ))

        return entities
    }

    // Define levels
    val tutorial = LevelData(
        playerStart = Vector2(0f, 5.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),  // Top
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),      // Bottom
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),   // Left
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),     // Right

            // Inner
            LevelWall(Vector2(-2.5f, -1f), Vector2(2.5f, -1f)),
        ),
        holes = listOf(

        ),
        goals = listOf(
            LevelGoal(Vector2(0f, -7.5f))
        )
    )

    val level1 = LevelData(
        playerStart = Vector2(0f, 5.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),  // Top
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),      // Bottom
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),   // Left
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),     // Right

            // Inner
            LevelWall(Vector2(-2f, 3f), Vector2(4.5f, 3f)),
            LevelWall(Vector2(0.5f, -1f), Vector2(0.5f, 2.5f)),
            LevelWall(Vector2(-3f, -1f), Vector2(0f, -1f)),
            LevelWall(Vector2(-4.5f, 1f), Vector2(-1.5f, 1f)),
            LevelWall(Vector2(2.5f, 1f), Vector2(4.5f, 1f)),
            LevelWall(Vector2(-4.5f, -3f), Vector2(3f, -3f)),
            LevelWall(Vector2(3f, -2.5f), Vector2(3f, -1f)),
            LevelWall(Vector2(2.5f, -1f), Vector2(2.5f, -1f)),
            LevelWall(Vector2(-2f, -5f), Vector2(4.5f, -5f)),
        ),
        holes = listOf(

        ),
        goals = listOf(
            LevelGoal(Vector2(0f, -7.5f))
        )
    )

    val level2 = LevelData(
        playerStart = Vector2(0f, 5.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),

            // Inner
            LevelWall(Vector2(-4.5f, 3.5f), Vector2(-3f, 3.5f)),
            LevelWall(Vector2(-2.5f, 3f), Vector2(-2.5f, 4f)),
            LevelWall(Vector2(2.5f, 3f), Vector2(2.5f, 4f)),
            LevelWall(Vector2(3f, 3.5f), Vector2(4.5f, 3.5f)),
            LevelWall(Vector2(0f, 0.5f), Vector2(0f, 2f)),
            LevelWall(Vector2(-4.5f, 0f), Vector2(2.5f, 0f)),
            LevelWall(Vector2(-4.5f, -3.5f), Vector2(-3f, -3.5f)),
            LevelWall(Vector2(-2.5f, -4f), Vector2(-2.5f, -3f)),
            LevelWall(Vector2(2.5f, -4f), Vector2(2.5f, -3f)),
            LevelWall(Vector2(3f, -3.5f), Vector2(4.5f, -3.5f)),
            LevelWall(Vector2(-1f, -5.5f), Vector2(0.5f, -5.5f)),
            LevelWall(Vector2(0.5f, -8f), Vector2(0.5f, -6f)),
            LevelWall(Vector2(2.5f, -7f), Vector2(4.5f, -7f)),
        ),
        holes = listOf(
            LevelHole(Vector2(-0.5f, -6.5f)),
            LevelHole(Vector2(4f, -6f)),
            LevelHole(Vector2(-4f, -2.5f)),
            LevelHole(Vector2(4f, 2.5f)),
            LevelHole(Vector2(-4f, 7f)),
        ),
        goals = listOf(
            LevelGoal(Vector2(3.5f, -8.5f))
        )
    )

    val level3 = LevelData(
        playerStart = Vector2(-3.5f, 6.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),

            // Zigzag pattern
            LevelWall(Vector2(-5f, -6f), Vector2(2f, -6f)),
            LevelWall(Vector2(-2f, -2f), Vector2(5f, -2f)),
            LevelWall(Vector2(-5f, 2f), Vector2(2f, 2f)),
            LevelWall(Vector2(-2f, 6f), Vector2(5f, 6f))
        ),
        holes = listOf(
            LevelHole(Vector2(-4f, -4f)),
            LevelHole(Vector2(4f, 0f)),
            LevelHole(Vector2(-4f, 4f))
        ),
        goals = listOf(
            LevelGoal(Vector2(-3.5f, -8.5f))
        )
    )

    val level4 = LevelData(
        playerStart = Vector2(-3f, 0.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),

            // Zigzag pattern
            LevelWall(Vector2(-5f, -6f), Vector2(2f, -6f)),
            LevelWall(Vector2(-2f, -2f), Vector2(5f, -2f)),
            LevelWall(Vector2(-5f, 2f), Vector2(2f, 2f)),
            LevelWall(Vector2(-2f, 6f), Vector2(5f, 6f))
        ),
        holes = listOf(
            LevelHole(Vector2(-4f, -4f)),
            LevelHole(Vector2(4f, 0f)),
            LevelHole(Vector2(-4f, 4f))
        ),
        goals = listOf(
            LevelGoal(Vector2(-3.5f, -2.5f))
        )
    )

    val level5 = LevelData(
        playerStart = Vector2(3.5f, 6.5f),
        walls = listOf(
            // Outer bounds
            LevelWall(Vector2(-5f, -10f), Vector2(5f, -10f)),
            LevelWall(Vector2(-5f, 8f), Vector2(5f, 8f)),
            LevelWall(Vector2(-5f, -10f), Vector2(-5f, 8f)),
            LevelWall(Vector2(5f, -10f), Vector2(5f, 8f)),

            // Zigzag pattern
            LevelWall(Vector2(-5f, -6f), Vector2(2f, -6f)),
            LevelWall(Vector2(-2f, -2f), Vector2(5f, -2f)),
            LevelWall(Vector2(-5f, 2f), Vector2(2f, 2f)),
            LevelWall(Vector2(-2f, 6f), Vector2(5f, 6f))
        ),
        holes = listOf(
            LevelHole(Vector2(-4f, -4f)),
            LevelHole(Vector2(4f, 0f)),
            LevelHole(Vector2(-4f, 4f))
        ),
        goals = listOf(
            LevelGoal(Vector2(-3.5f, -6f))
        )
    )
}