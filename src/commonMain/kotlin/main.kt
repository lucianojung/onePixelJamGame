import com.soywiz.klock.*
import com.soywiz.klogger.AnsiEscape
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
import com.soywiz.korim.bitmap.effect.BitmapEffect
import com.soywiz.korim.color.*
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
        val circle = circle(100.0, Colors.BLUE).xy(300, 100)
        circle.onClick {
            sceneContainer.changeTo<Scene2>()
        }
    }
}

class Scene2() : Scene() {
    override suspend fun Container.sceneInit() {

        var bool = true

        val circle  = circle(25.0, Colors.WHITE).xy(256, 900)
        circle.onClick {
                circle.color = Colors.RED

        }
        val rect = roundRect(150.0, 50.0, 0.0, 0.0, Colors.BLACK, Colors.WHITE, 4.0, true).xy(106, 1000)
        rect.onClick {
        }

        val rect2 = roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE, Colors.BLACK, 4.0, true).xy(256, 1000)
        rect.onClick {

        }


        val font = BitmapFont(
                DefaultTtfFont, 24.0,
                paint = Colors.WHITE,

                )
        val font2 = BitmapFont(
                DefaultTtfFont, 24.0,
                paint = Colors.BLACK,

                )

        text("Left", font = font, textSize = 24.0, alignment = TextAlignment.BASELINE_LEFT, ).position(160, 1030)

        text("Right", font = font2, textSize = 24.0, alignment = TextAlignment.BASELINE_LEFT, ).position(310, 1030)





      /*  uiTextButton(256.0, 32.0) {
            text = "Right Button"
            textColor = Colors.GREEN
            position(256, 128)
            onClick {
                println("Right")
            }
            disable()
        }

       */
    }
}