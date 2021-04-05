import com.soywiz.korge.*
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korim.color.*
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.*
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = ConfigModule))


object ConfigModule : Module() {
    override val bgcolor = Colors["#000000"]
    override val size = SizeInt(512, 1080)
    override val mainScene: KClass<out Scene> = Scene1::class
    override val title: String
        get() = "1Bit Jam"

    override suspend fun AsyncInjector.configure() {
        mapPrototype { Scene1() }
        mapPrototype { Scene2() }
        mapPrototype { Scene3() }
    }
}
