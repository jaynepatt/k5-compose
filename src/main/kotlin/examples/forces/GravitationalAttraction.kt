package examples.forces

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerMoveFilter
import k5
import math.Vector2D
import math.add
import math.constrain
import math.divide
import math.k5Random
import math.magSq
import math.multiply
import math.set
import math.setMag
import math.sub
import math.toOffSet
import kotlin.math.sqrt
import kotlin.random.Random

data class Moon(val x: Float, val y: Float, val m: Float) {
    val position = Vector2D(x, y)
    val velocity = Vector2D.randomVector()
    val acceleration = Vector2D(0f, 0f)
    val mass = m
    val r = sqrt(mass) * 2

    init {
        velocity.multiply(5f)
    }

    fun applyForce(force: Vector2D) {
        val f = force.divide(mass)
        acceleration.add(f)
    }

    fun update() {
        velocity.add(acceleration)
        position.add(velocity)
        acceleration.multiply(0f)
    }

    fun render(drawScope: DrawScope) {
        drawScope.drawOval(Color.White, position.toOffSet(), Size(r * 2, r * 2))
    }
}

data class Attractor(val x: Float, val y: Float, val m: Float) {

    val position = Vector2D(x, y)
    val mass = m
    val radius = sqrt(mass) * 2

    fun attract(ball: Moon) {
        val attractorPosition = position.copy()
        val force = attractorPosition.sub(ball.position)
        val distanceSq = constrain(force.magSq(), 100f, 1000f)

        val G = 8 // Universal Gravitational Constant

        val gPull = G * (mass * ball.mass) / distanceSq

        force.setMag(gPull)

        ball.applyForce(force)
    }

    fun render(drawScope: DrawScope) {
        drawScope.drawOval(Color.Magenta, position.toOffSet(), Size(radius * 2, radius * 2))
    }
}

fun gravitationalPull() = k5 {

    val moons = mutableListOf<Moon>()
    repeat(15) {
        moons.add(
            Moon(
                k5Random(50, (getPlaygroundDimensions().width).toInt()),
                k5Random(50, getPlaygroundDimensions().height.toInt()),
                Random.nextInt(20, 40).toFloat()
            )
        )
    }

    val attractor = Attractor(400f, 400f, 200f)

    show(
        modifier = Modifier.pointerMoveFilter(
            onMove = {
                attractor.position.set(Vector2D(it.x, it.y))
                false
            }
        )
    ) { dt, drawScope ->
        attractor.render(drawScope)
        for (moon in moons) {
            moon.update()
            moon.render(drawScope)
            attractor.attract(moon)
        }
    }
}