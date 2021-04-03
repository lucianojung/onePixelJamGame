import com.soywiz.klock.timesPerSecond
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.text.CreateStringTextRenderer
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.plus
import com.soywiz.korma.geom.sin

class Scene1() : Scene() {
    override suspend fun Container.sceneInit() {
        val circle = circle(100.0, Colors.WHITE).xy(156, 600)

        circle.onClick {
            sceneContainer.changeTo<Scene2>()
        }

        roundRect(400.0, 50.0, 5.0, 5.0, Colors.BLACK, Colors.WHITE, 4.0, true).xy(60, 800)

        val font = BitmapFont(
                DefaultTtfFont, 128.0,
                paint = Colors.WHITE,
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
        }).position(15, 300)

        text("Luciano Jung & Maria Lorenz", font = font, textSize = 24.0, alignment = TextAlignment.BASELINE_LEFT).position(120,830)
    }
}