package com.example.labyrinthmarblegame

data class Circle(
    val position: Vector2,
    var radius: Float
)

data class Rectangle(
    val position: Vector2,  // Center position
    val width: Float,
    val height: Float
)

fun GameEntity.toCircle(): Circle {
    return Circle(position, scale.x / 2f)
}

fun GameEntity.toRectangle(): Rectangle {
    return Rectangle(position, scale.x, scale.y)
}

class CollisionSystem {
    data class CollisionInfo(
        val collisionPoint: Vector2,
        val normal: Vector2  // Direction of collision
    )

    fun checkCircleRectangleCollision(circle: Circle, rect: Rectangle): CollisionInfo? {
        // Calculate the closest point on the rectangle to the circle
        val closestX = circle.position.x.coerceIn(
            rect.position.x - rect.width / 2,
            rect.position.x + rect.width / 2
        )
        val closestY = circle.position.y.coerceIn(
            rect.position.y - rect.height / 2,
            rect.position.y + rect.height / 2
        )

        val distanceX = circle.position.x - closestX
        val distanceY = circle.position.y - closestY
        val distanceSquared = distanceX * distanceX + distanceY * distanceY

        if (distanceSquared <= circle.radius * circle.radius) {
            // Determine collision normal
            val normal = when {
                // Hit top or bottom edge
                closestX != circle.position.x && closestY == rect.position.y + rect.height / 2 -> Vector2(0f, 1f)
                closestX != circle.position.x && closestY == rect.position.y - rect.height / 2 -> Vector2(0f, -1f)
                // Hit left or right edge
                closestY != circle.position.y && closestX == rect.position.x + rect.width / 2 -> Vector2(1f, 0f)
                closestY != circle.position.y && closestX == rect.position.x - rect.width / 2 -> Vector2(-1f, 0f)
                // Corner collision - use normalized direction from closest point to circle center
                else -> {
                    val length = kotlin.math.sqrt(distanceSquared)
                    Vector2(distanceX / length, distanceY / length)
                }
            }
            return CollisionInfo(Vector2(closestX, closestY), normal)
        }
        return null
    }

    fun checkCircleCircleCollision(circle1: Circle, circle2: Circle): Boolean {
        val dx = circle2.position.x - circle1.position.x
        val dy = circle2.position.y - circle1.position.y
        val distanceSquared = dx * dx + dy * dy
        val radiusSum = circle1.radius + circle2.radius
        return distanceSquared <= radiusSum * radiusSum
    }
}

// Helper extension function
fun Vector2.length(): Float {
    return kotlin.math.sqrt(x * x + y * y)
}