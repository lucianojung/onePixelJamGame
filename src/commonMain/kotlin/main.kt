import com.soywiz.klock.*
import com.soywiz.klogger.AnsiEscape
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
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
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*


suspend fun main() = Korge(width = 540, height = 1080, bgcolor = Colors["#000000"]) {

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

