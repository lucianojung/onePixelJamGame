import com.soywiz.klock.*
import com.soywiz.klogger.AnsiEscape
import com.soywiz.korev.addEventListener
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.service.process.NativeProcess
import com.soywiz.korge.tiled.colorFromARGB
import com.soywiz.korge.time.frameBlock
import com.soywiz.korge.tween.*
import com.soywiz.korge.ui.defaultUIFont
import com.soywiz.korge.ui.defaultUISkin
import com.soywiz.korge.ui.uiCheckBox
import com.soywiz.korge.ui.uiTextButton
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.effect.BitmapEffect
import com.soywiz.korim.color.*
import com.soywiz.korim.color.Colors.BLACK
import com.soywiz.korim.color.Colors.BLUE
import com.soywiz.korim.color.Colors.GREEN
import com.soywiz.korim.color.Colors.RED
import com.soywiz.korim.color.Colors.WHITE
import com.soywiz.korim.color.Colors.YELLOW
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.*
import com.soywiz.korim.paint.LinearGradientPaint
import com.soywiz.korim.text.CreateStringTextRenderer
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlin.math.min
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = ConfigModule))


object ConfigModule : Module() {
    override val bgcolor = Colors["#000000"]
    override val size = SizeInt(512, 1080)
    override val mainScene: KClass<out Scene> = Scene2::class

    override suspend fun AsyncInjector.configure() {
        mapPrototype { Scene1() }
        mapPrototype { Scene2() }
    }
}

class Scene1() : Scene() {
    override suspend fun Container.sceneInit() {
        val circle = circle(100.0, Colors.WHITE).xy(156, 500)

        circle.onClick {
            sceneContainer.changeTo<Scene2>()
        }

        val rect = roundRect(300.0, 50.0, 5.0, 5.0, Colors.BLACK, Colors.WHITE, 4.0, true).xy(106, 900)

        val font = BitmapFont(
                DefaultTtfFont, 128.0,
                paint = WHITE,
        )

        var offset = 0.degrees
        addFixedUpdater(60.timesPerSecond) { offset += 10.degrees }
        var version = 0
        text("1BIT JAM", font = font, textSize = 128.0, alignment = TextAlignment.BASELINE_LEFT, renderer = CreateStringTextRenderer({ version++ }) { text, n, c, c1, g, advance ->
            transform.identity()
            val sin = sin(offset + (n * 360 / text.length).degrees)
            transform.translate(0.0, sin * 8)
            transform.scale(1.0, 1.0 + sin * 0.1)
            put(c)
            advance(advance)
        }).position(12, 300)
    }


}

class Scene2() : Scene() {
    override suspend fun Container.sceneInit() {

        val jumpIntensity = 4.0
        val moveIntensity = 2.0
        val maxSpeed = 8.0
        val gravity = 0.05

        var leftWalk = false
        var horizontal = 1.0 * moveIntensity
        var vertical = 0.0

        // making ground objects
        // size = 512 x 1028
        var groundObjects: MutableList<ShapeView> = createGroundObjects(this)

        val wallLeft = roundRect(50.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(-40, 0)
        val wallRight = roundRect(50.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(502, 0)
        val menubar = roundRect(1000.0, 200.0, 0.0, 0.0, Colors["#000000"], Colors["#000000"], 0.0, true).xy(0, 949)

        val circle = circle(25.0, Colors.WHITE).xy(256, 700)
        circle.onClick {
            circle.color = Colors.RED
        }
        circle.addUpdater {
            // update ball gravity
            vertical -= gravity

            //bounce on border collission
            if (circle.x < 0.0 || circle.x > 512 - circle.radius * 2) {
                horizontal -= horizontal * 2
            }

            //check Collission
            var collission = colliding(this, groundObjects)
            if (collission != 0) {
                vertical = collission * jumpIntensity
            }

            // clamp max Ball Speed in all Vertical Direction
            vertical = min(horizontal * vertical, maxSpeed) / horizontal

            // set ball values
            if (leftWalk) {
                circle.xy(circle.x - horizontal, circle.y - vertical)
            } else {
                circle.xy(circle.x + horizontal, circle.y - vertical)
            }

            updateShapePositions(this, groundObjects)

            if(y > 1080) {
                //handle Death
            }

        }

        var bool = true

        val rect2 = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#000000"], 4.0, true).xy(256, 1000)
        val rect = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#000000"] , Colors["#FFFFFF"], 4.0, true).xy(106, 1000)

        rect.onClick {
            changeButtonColor(bool, rect, rect2)
            bool =! bool
            leftWalk = !leftWalk
        }

        rect2.onClick {
            changeButtonColor(bool, rect, rect2)
            bool =! bool
            leftWalk = !leftWalk
        }

        val font = BitmapFont(
                DefaultTtfFont, 24.0,
                paint = Colors.WHITE,

                )
        val font2 = BitmapFont(
                DefaultTtfFont, 24.0,
                paint = Colors.BLACK,

                )

    }



    private fun changeButtonColor(bool: Boolean, rect: RoundRect, rect2: RoundRect) {
        if(bool) {
            rect.fill = Colors["#FFFFFF"]
            rect.stroke = Colors["#000000"]
            rect2.fill = Colors["#000000"]
            rect2.stroke = Colors["#FFFFFF"]
        }
        else {
            rect.fill = Colors["#000000"]
            rect.stroke = Colors["#FFFFFF"]
            rect2.fill = Colors["#FFFFFF"]
            rect2.stroke = Colors["#000000"]
        }
    }

    private suspend fun createGroundObjects(container: Container): MutableList<ShapeView> {
        val bitmap : Bitmap = resourcesVfs["chest_white.png"].readBitmap()
        val baseground = container.roundRect(1000.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(0, 900)
        val ground1 = container.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(0, 750)
        val ground2 = container.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -200, 650)
        val ground3 = container.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -350, 500)
        val ground4 = container.roundRect(250.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -250, 300)
        val image = container.image(bitmap).scale(0.5).position(112,650)
        val ground5 = container.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, 400)
        val ground6 = container.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -250, 50)
        val ground7 = container.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 450, 150)
        val ground8 = container.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -400, -100)
        val ground9 = container.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, -250)
        val ground10 = container.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -350, -350)
        val ground11 = container.roundRect(80.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 80, -500)
        val ground12 = container.roundRect(250.0, 50.0, 0.0, 0.0, Colors.YELLOW).xy(512 -512, -650)
        val ground13 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.RED).xy(512 - 50, -850)
        val ground14 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.BLUE).xy(512 - 50, -850)
        val ground15 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.VIOLET).xy(512 - 50, -850)
        val ground16 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GREEN).xy(512 - 50, -850)
        val ground17 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKCYAN).xy(512 - 50, -850)
        val ground18 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GOLDENROD).xy(512 - 50, -850)
        val ground19 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKGREEN).xy(512 - 50, -850)
        val ground20 = container.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKMAGENTA).xy(512 - 50, -850)

        return mutableListOf(baseground, ground1, ground2,ground3,ground4,ground5, ground6, ground7, ground8,ground9,ground10,ground11,ground12,ground13)
    }


    private fun updateShapePositions(circle: Circle, groundObjects: MutableList<ShapeView>) {
        if (circle.y < 540) {
            val difference = circle.y - 540.0
            circle.y -= difference
            groundObjects.forEach { shape ->
                shape.y -= difference
            }
        }
    }

    private fun colliding(circle: ShapeView, groundObjects: MutableList<ShapeView>): Int {
        groundObjects.forEach { shape ->
            if (circle.collidesWith(shape)) {
                if (circle.y < shape.y)
                    return 1
                else
                    return -1
            }
        }
        return 0
    }
}