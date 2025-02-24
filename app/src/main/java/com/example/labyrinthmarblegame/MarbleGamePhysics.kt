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
    // Use the smaller dimension to form the circle's diameter (or adjust as needed)
    val diameter = scale.x.coerceAtMost(scale.y)
    return Circle(position, diameter / 2f)
}

fun GameEntity.toRectangle(): Rectangle {
    return Rectangle(position, scale.x, scale.y)
}

class CollisionSystem {
    data class CollisionInfo(
        val collisionPoint: Vector2,
        val normal: Vector2,  // Direction of collision
        val penetration: Float
    )

    fun checkCircleRectangleCollision(circle: Circle, rect: Rectangle): CollisionInfo? {
        // Calculate rectangle half extents
        val halfWidth = rect.width / 2f
        val halfHeight = rect.height / 2f

        // Difference vector from rectangle center to circle center
        val diffX = circle.position.x - rect.position.x
        val diffY = circle.position.y - rect.position.y

        // Clamp the difference to the rectangle's half extents to get the closest point
        val clampedX = diffX.coerceIn(-halfWidth, halfWidth)
        val clampedY = diffY.coerceIn(-halfHeight, halfHeight)

        // Compute the closest point on the rectangle (in world space)
        val closestX = rect.position.x + clampedX
        val closestY = rect.position.y + clampedY

        // Vector from the closest point to the circle center
        val vectorX = circle.position.x - closestX
        val vectorY = circle.position.y - closestY

        val distanceSquared = vectorX * vectorX + vectorY * vectorY

        // No collision if the closest distance is greater than the circle's radius
        if (distanceSquared > circle.radius * circle.radius) {
            return null
        }

        val distance = kotlin.math.sqrt(distanceSquared)
        // Compute the collision normal; avoid division by zero
        val normal = if (distance != 0f) {
            Vector2(vectorX / distance, vectorY / distance)
        } else {
            // Circle center is inside the rectangle.
            // Determine penetration in both axes and choose the smaller one.
            val penetrationX = halfWidth - kotlin.math.abs(diffX)
            val penetrationY = halfHeight - kotlin.math.abs(diffY)
            if (penetrationX < penetrationY) {
                Vector2(if (diffX < 0) -1f else 1f, 0f)
            } else {
                Vector2(0f, if (diffY < 0) -1f else 1f)
            }
        }
        // Compute penetration depth
        val penetration = if (distance != 0f) {
            circle.radius - distance
        } else {
            kotlin.math.min(halfWidth - kotlin.math.abs(diffX), halfHeight - kotlin.math.abs(diffY))
        }
        return CollisionInfo(Vector2(closestX, closestY), normal, penetration)
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
