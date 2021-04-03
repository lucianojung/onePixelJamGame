import com.soywiz.klock.Frequency
import com.soywiz.klock.milliseconds
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.delay
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.Thread_sleep
import kotlin.math.min
import kotlin.random.Random

class Scene2() : Scene() {

    private var jumpIntensity = 4.0
    private var moveIntensity = 2.0
    private var maxSpeed = 8.0
    private var gravity = 0.05

    private var pixeldepth = 0.0
    private var powerUpTime = 300
    private var powerUpActive: Boolean = false

    private var leftWalk = false
    private var playerIsAlive = false

    private var mainContainer: Container = Container()
    private var player: Circle = Circle()
    private var groundObjects: MutableList<ShapeView> = mutableListOf()
    private var treasureObjects: MutableList<Image> = mutableListOf()
    private var infotext: Text = Text("")


    override suspend fun Container.sceneInit() {
        mainContainer = this

        // making ground objects
        // size = 512 x 1028
        groundObjects = createGroundObjects()
        treasureObjects = createTreasureObjects()

        initPlayer()

        //walls and player control area
        roundRect(10.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(0, 0)
        roundRect(10.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(502, 0)

        roundRect(1000.0, 200.0, 0.0, 0.0, Colors["#000000"], Colors["#000000"], 0.0, true).xy(0, 949)
        val buttonLeft = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#000000"], 4.0, true).xy(256, 1000)
        val buttonRight = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#000000"], Colors["#FFFFFF"], 4.0, true).xy(106, 1000)

        buttonRight.onClick {
            changeButtonColor(leftWalk, buttonRight, buttonLeft)
            leftWalk = !leftWalk
        }

        buttonLeft.onClick {
            changeButtonColor(leftWalk, buttonRight, buttonLeft)
            leftWalk = !leftWalk
        }

        infotext = mainContainer.text("", font = BitmapFont(
                DefaultTtfFont, 32.0,
                paint = Colors.WHITE,
        ), textSize = 32.0, alignment = TextAlignment.CENTER)
        infotext.position(256, 0)
    }

    private fun initPlayer() {

        var horizontal = 1.0 * moveIntensity
        var vertical = 0.0

        player = mainContainer.circle(25.0, Colors.WHITE).xy(256, 700)
        player.onClick {
            player.color = Colors.RED
        }
        player.addFixedUpdater(Frequency(hertz = 60.0)) {
            // update ball gravity
            vertical -= gravity

            //bounce on border collission
            if (player.x < 10.0 || player.x > 502 - player.radius * 2) {
                horizontal -= horizontal * 2
            }

            //check Collission
            val collission = collidingGround()
            if (collission != 0) {
                vertical = collission * jumpIntensity
            }
            //check for powerup collection
            if (collidingPowerUps()) {
                activateRandomPowerUp()
            }

            //PowerUp Handling
            if (powerUpActive){
                powerUpTime--
            }
            if (powerUpTime <= 0) {
                infotext.setText("")
                powerUpActive = false
                deleteAllPowerUps()
            }

            // clamp max Ball Speed in all Vertical Direction
            vertical = min(horizontal * vertical, maxSpeed) / horizontal

            // set ball values
            if (leftWalk) {
                player.xy(player.x - horizontal, player.y - vertical)
            } else {
                player.xy(player.x + horizontal, player.y - vertical)
            }

            updateShapePositions()

            if (player.y > 950 && playerIsAlive) {
                playerIsAlive = false
                handleGameOver()
            }
        }
    }

    private fun handleGameOver() {
        mainContainer.solidRect(512.0, 1080.0, Colors["#000000"]).xy(0, 0)
        mainContainer.text("Restart Game", textSize = 32.0, alignment = TextAlignment.BASELINE_LEFT).position(156, 500)
        val restartButton = mainContainer.roundRect(400.0, 100.0, 5.0, 5.0, Colors.TRANSPARENT_BLACK, Colors.WHITE, 4.0, true).xy(106, 450)

        restartButton.onClick {
            playerIsAlive = true
            sceneContainer.changeTo<Scene2>()
        }

        val font = BitmapFont(
                DefaultTtfFont, 64.0,
                paint = Colors.WHITE,
        )
        mainContainer.text("Game Over!", font = font, textSize = 64.0, alignment = TextAlignment.BASELINE_LEFT).position(12, 300)
    }

    private fun activateRandomPowerUp() {
        val powerUpNumber = (Random.nextDouble() * 6).toInt()
        when (powerUpNumber) {
            1 -> {
                infotext.setText("Gravity decreased")
                gravity -= 0.005
            }
            2 -> {
                infotext.setText("Gravity increased")
                gravity += 0.02
            }
            3 -> {
                infotext.setText("Sideway move speed increased")
                moveIntensity += 0.5
            }
            4 -> {
                infotext.setText("Sideway move speed decreased")
                moveIntensity -= 0.5
            }
            5 -> {
                infotext.setText("Jumping Intensity decreased")
                jumpIntensity -= 0.5
            }
            else -> {
                infotext.setText("Jumping Intensity increased")
                jumpIntensity += 0.5
            }
        }
        powerUpActive = true
        powerUpTime = 300
    }

    private fun deleteAllPowerUps() {
        jumpIntensity = 4.0
        moveIntensity = 2.0
        maxSpeed = 8.0
        gravity = 0.05
    }

    private fun changeButtonColor(bool: Boolean, rect: RoundRect, rect2: RoundRect) {
        if (bool) {
            rect.fill = Colors["#FFFFFF"]
            rect.stroke = Colors["#000000"]
            rect2.fill = Colors["#000000"]
            rect2.stroke = Colors["#FFFFFF"]
        } else {
            rect.fill = Colors["#000000"]
            rect.stroke = Colors["#FFFFFF"]
            rect2.fill = Colors["#FFFFFF"]
            rect2.stroke = Colors["#000000"]
        }
    }

    private fun createGroundObjects(): MutableList<ShapeView> {
        val baseground = mainContainer.roundRect(1000.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(0, 900)
        val ground1 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(0, 750)
        val ground2 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 200, 650)
        val ground3 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 350, 500)
        val ground4 = mainContainer.roundRect(250.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 250, 300)
        val ground5 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, 400)
        val ground6 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -250, 50)
        val ground7 = mainContainer.roundRect(250.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, 150)
        val ground8 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -400, -100)
        val ground82 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.GREEN).xy(512 -150, 100)
        val ground9 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, -250)

        val ground102 = mainContainer.roundRect(250.0, 50.0, 0.0, 0.0, Colors.RED).xy(512 -512, -350)
        val ground11 = mainContainer.roundRect(80.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 80, -500)

        val ground17 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 150, -200)
        val ground12 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.ORANGE).xy(512 -400, -600)
        val ground13 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 100, -750)
        val ground10 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -512, -750)
        val ground14 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.BLUE).xy(512 - 512, -850)

        val ground19 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -150, -1000)
        val ground20 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, -1150)

        val ground18 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, -1400)
        val ground23 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 100, -1250)
        val ground24 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -400, -1350)
        val ground25 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GOLDENROD).xy(512 - 50, -1500)
        val ground26 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKGREEN).xy(512 - 50, -1600)
        val ground27 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKMAGENTA).xy(512 - 50, -1750)
        val ground28 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GOLDENROD).xy(512 - 50, -1850)
        val ground29 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKGREEN).xy(512 - 50, -2000)
        val ground15 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.VIOLET).xy(512 - 512, -2100)
        val ground16 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.GREEN).xy(512 - 100, -2200)
        val ground21 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.GREEN).xy(512 -150, -2300)



        return mutableListOf(baseground, ground1, ground2,ground3,ground4,ground5, ground6, ground7, ground8,ground82,ground9,ground10,ground102,ground11,ground12,ground13,ground14,ground15,ground16,ground17,ground18,ground19,ground20,ground21,ground23,ground24,ground25,ground26,ground27,ground28,ground29)
    }

    private suspend fun createTreasureObjects(): MutableList<Image> {
        val bitmap: Bitmap = resourcesVfs["chest_white.png"].readBitmap()
        val image1 = mainContainer.image(bitmap).scale(0.4).position(52, 705)
        val image2 = mainContainer.image(bitmap).scale(0.4).position(450, 55)
        val image3 = mainContainer.image(bitmap).scale(0.4).position(5, -795)

        return mutableListOf(image1,image2,image3)
    }

    private fun updateShapePositions() {
        if (player.y < 540) {
            val difference = player.y - 540.0
            player.y -= difference
            groundObjects.forEach { shape ->
                shape.y -= difference
            }
            treasureObjects.forEach { shape ->
                shape.y -= difference
            }
            pixeldepth += difference
        }
    }

    private fun collidingPowerUps(): Boolean {
        treasureObjects.forEach { shape ->
            if (player.collidesWith(shape)) {
                shape.removeFromParent()
                return true
            }
        }
        return false
    }

    private fun collidingGround(): Int {
        groundObjects.forEach { shape ->
            if (player.collidesWith(shape)) {
                if (player.y < shape.y)
                    return 1
                else
                    return -1
            }
        }
        return 0
    }
}