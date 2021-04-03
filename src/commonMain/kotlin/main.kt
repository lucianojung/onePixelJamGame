import com.soywiz.klock.*
import com.soywiz.klogger.AnsiEscape
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.service.process.NativeProcess
import com.soywiz.korge.tiled.colorFromARGB
import com.soywiz.korge.tween.*
import com.soywiz.korge.ui.defaultUIFont
import com.soywiz.korge.ui.defaultUISkin
import com.soywiz.korge.ui.uiCheckBox
import com.soywiz.korge.ui.uiTextButton
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.*
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.*
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = ConfigModule))


object ConfigModule : Module() {
	override val bgcolor = Colors["#2b2b2b"]
	override val size = SizeInt(512, 512)
	override val mainScene: KClass<out Scene> = Scene1::class

	override suspend fun AsyncInjector.configure(){
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
		uiTextButton(256.0, 32.0) {
			text = "Left Button"
			position(128, 128)
			onClick {
				println("Left")
			}
			disable()
		}
		uiTextButton(256.0, 32.0 ) {
			text = "Right Button"
			position(256, 128)
			onClick {
				println("Right")
			}
			disable()
		}
	}
}