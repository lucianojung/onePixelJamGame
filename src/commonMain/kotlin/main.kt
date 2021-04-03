import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.*
import com.soywiz.korim.color.Colors.WHITE
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.format.*
import com.soywiz.korim.text.CreateStringTextRenderer
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import kotlin.math.min
import kotlin.random.Random
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = ConfigModule))


object ConfigModule : Module() {
    override val bgcolor = Colors["#000000"]
    override val size = SizeInt(512, 1080)
    override val mainScene: KClass<out Scene> = Scene1::class

    override suspend fun AsyncInjector.configure() {
        mapPrototype { Scene1() }
        mapPrototype { Scene2() }
    }
}
