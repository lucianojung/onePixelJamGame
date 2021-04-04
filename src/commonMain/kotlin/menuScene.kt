import com.soywiz.klock.timesPerSecond
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.CreateStringTextRenderer
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.plus
import com.soywiz.korma.geom.sin

class Scene1() : Scene() {
    override suspend fun Container.sceneInit() {


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
        }).position(20, 300)


        text("trial", font = font, textSize = 48.0, alignment = TextAlignment.MIDDLE_RIGHT).position(210,500)
        text("endless", font = font, textSize = 48.0, alignment = TextAlignment.MIDDLE_LEFT).position(275,500)

        val bitmap: Bitmap = resourcesVfs["arrow_white.png"].readBitmap()
        val image = image(bitmap).scale(0.03).position(155, 555)
        val image2 = image(bitmap).scale(0.03).position(330, 555)

        val challengeCircle = circle(50.0, Colors.WHITE).xy(125, 707)
        challengeCircle.onClick {
            sceneContainer.changeTo<Scene2>()
        }
        val endlessCircle = circle(50.0, Colors.WHITE).xy(300, 707)
        endlessCircle.onClick {
            sceneContainer.changeTo<Scene3>()
        }

        roundRect(350.0, 70.0, 5.0, 5.0, Colors.TRANSPARENT_BLACK, Colors.WHITE, 4.0, true).xy(90, 807)
        text("Made by", font = font, textSize = 20.0, alignment = TextAlignment.TOP_CENTER).position(265,815)
        text("Luciano Jung & Maria Lorenz", font = font, textSize = 24.0, alignment = TextAlignment.TOP_CENTER).position(265,835)

    }
}