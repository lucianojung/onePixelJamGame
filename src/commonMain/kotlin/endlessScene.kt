import com.soywiz.klock.Frequency
import com.soywiz.klock.seconds
import com.soywiz.korau.sound.readSound
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.delay
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.ColorTransform
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.math.min
import kotlin.random.Random

class Scene3() : Scene() {

    private var multiplikator = 1.0
    private var multiplikator2 = 1.0

    private var jumpIntensity = 4.0
    private var moveIntensity = 2.0
    private var maxSpeed = 9.0
    private var gravity = 0.05
    private val plattformGap = 150
    private var currentHeight = 900

    private var pixeldepth = 0.0
    private var powerUpTime = 300
    private var powerUpActive: Boolean = false
    private var soundPlaying: Boolean = false

    private var leftWalk = false
    private var playerIsAlive = true

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
        groundObjects[0].addUpdater {
            for (i in 1 until groundObjects.size) {
                if (groundObjects[i].y > 1000) {
                    var index = (i - 1 + groundObjects.size) % groundObjects.size
                    if (index == 0)
                        index = groundObjects.size - 1
                    groundObjects[i].removeFromParent()
                    groundObjects[i] = nextPlattform(groundObjects[index])
                }
            }
        }

        initPlayer()

        //create exit Button and Listener
        roundRect(49.0, 49.0, 5.0, 5.0, Colors.BLACK).xy(20, 0)
        val exitBitmap: Bitmap = resourcesVfs["exit.png"].readBitmap()
        val exitButton = image(exitBitmap).scale(0.01).position(28, 8)
        exitButton.onClick {
            sceneContainer.changeTo<Scene1>()
        }
        roundRect(452.0, 50.0, 5.0, 5.0, Colors.TRANSPARENT_BLACK).xy(60, 0)
        //walls and player control area
        roundRect(10.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(0, 0)
        roundRect(10.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(502, 0)

        roundRect(1000.0, 200.0, 0.0, 0.0, Colors["#000000"], Colors["#000000"], 0.0, true).xy(0, 949)
        val buttonLeft = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#000000"], 4.0, true).xy(256, 1000)
        val buttonRight = roundRect(150.0, 50.0, 0.0, 0.0, Colors["#000000"], Colors["#FFFFFF"], 4.0, true).xy(106, 1000)

        buttonRight.onClick {
            changeButtonColor(buttonRight, buttonLeft)
            leftWalk = !leftWalk
        }

        buttonLeft.onClick {
            changeButtonColor(buttonRight, buttonLeft)
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
            player.color = Colors.BLACK
        }

        player.addFixedUpdater(Frequency(hertz = 60.0)) {
            // update ball gravity
            vertical -= gravity

            //bounce on border collission
            if (player.x < 10.0 || player.x > 502 - player.radius * 2) {
                horizontal -= horizontal * 2
            }

            //PowerUp Handling
            if (powerUpActive) {
                powerUpTime--
            }
            if (powerUpTime <= 0) {
                infotext.setText("")
                powerUpActive = false
                deleteAllPowerUps()
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

    private fun playBounceSound() {
        launchImmediately {
            soundPlaying = true
            var sound = resourcesVfs["bounce.mp3"].readSound()
            sound.volume = 2000.0
            sound.play()
            delay(sound.length)
            soundPlaying = false
        }
    }

    private fun handleGameOver() {
        mainContainer.solidRect(512.0, 1080.0, Colors["#000000"]).xy(0, 0)
        mainContainer.text("Restart Game", textSize = 32.0, alignment = TextAlignment.CENTER).position(256, 530)
        val restartButton = mainContainer.roundRect(400.0, 100.0, 5.0, 5.0, Colors.TRANSPARENT_BLACK, Colors.WHITE, 4.0, true).xy(56, 500)

        restartButton.onClick {
            playerIsAlive = true
            sceneContainer.changeTo<Scene3>()
        }

        val font = BitmapFont(
                DefaultTtfFont, 64.0,
                paint = Colors.WHITE,
        )
        mainContainer.text("Game Over!", font = font, textSize = 64.0, alignment = TextAlignment.CENTER).position(256, 300)
        mainContainer.text("Pixelheight reached: " + (-1 * (pixeldepth + 1048 - player.y)).toInt().toString(), font = font, textSize = 32.0, alignment = TextAlignment.CENTER).position(256, 400)
    }

    private fun activateRandomPowerUp() {
        val powerUpNumber = (Random.nextInt(8))
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
            6 -> {
                infotext.setText("Gravity inverted")
                gravity = -0.05
            }
            7 -> {
                infotext.setText("color invert")
                groundObjects.forEach { shape ->
                    shape.fill = Colors["#000000"]
                }
                var rect = mainContainer.solidRect(512, 1080, Colors.WHITE)
                mainContainer.sendChildToBack(rect)
                player.fill = Colors.BLACK
                launchImmediately{
                    delay(5.seconds)
                    rect.removeFromParent()
                    groundObjects.forEach { shape ->
                        shape.fill = Colors["#FFFFFF"]
                    }
                    player.fill = Colors.WHITE
                }
            }
            8 -> {
                infotext.setText("Speed increased")
                jumpIntensity += 8.0
                moveIntensity += 8.0
                maxSpeed += 4.0

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
        multiplikator += 0.001
        multiplikator2 += 0.00005

        jumpIntensity = 4.0 * multiplikator2
        moveIntensity = 2.0 * multiplikator2
        maxSpeed = 8.0 * multiplikator
        gravity = 0.05
    }

    private fun changeButtonColor(rect: RoundRect, rect2: RoundRect) {
        if (rect.fill == Colors["#000000"]) {
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

        var ground0 = nextPlattform(baseground)
        var ground1 = nextPlattform(ground0)
        val ground2 = nextPlattform(ground1)
        val ground3 = nextPlattform(ground2)
        val ground4 = nextPlattform(ground3)
        val ground5 = nextPlattform(ground4)
        val ground6 = nextPlattform(ground5)
        val ground7 = nextPlattform(ground6)
        val ground8 = nextPlattform(ground7)
        val ground9 = nextPlattform(ground8)

        return mutableListOf(baseground, ground0, ground1, ground2, ground3, ground4, ground5, ground6, ground7, ground8, ground9)
    }

    private fun nextPlattform(lastPlattform: ShapeView): ShapeView {
        var newPlattform = ShapeView()

        do {
            val start = Random.nextDouble(462.0) + 20
            val width = min(Random.nextDouble(350.0) + 50, 462 - start)

            newPlattform.removeFromParent()
            newPlattform = mainContainer.roundRect(width.toDouble(), 50.0, 0.0, 0.0, Colors.WHITE).xy(start.toInt(), (lastPlattform.y - plattformGap).toInt())
        } while (
                (lastPlattform.x + player.radius * 2 > newPlattform.x) &&
                (lastPlattform.x + lastPlattform.scaledWidth - player.radius * 2 < newPlattform.x + newPlattform.scaledWidth) &&
                (lastPlattform.x - newPlattform.x - newPlattform.scaledWidth < 250 ) &&
                (newPlattform.x - lastPlattform.x - lastPlattform.scaledWidth < 250 )
        )
        //calculate TreasureChest
        if (Random.nextInt(5) == 0) {
            launchImmediately {
                val bitmap: Bitmap = resourcesVfs["chest_white.png"].readBitmap()
                val image = mainContainer.image(bitmap).scale(0.4).position(Random.nextInt(newPlattform.width.toInt()) + newPlattform.x.toInt(), (lastPlattform.y - plattformGap - 45).toInt())
                treasureObjects.add(image)
                mainContainer.sendChildToBack(image)
            }
        }
        mainContainer.sendChildToBack(newPlattform)
        return newPlattform
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
                if (!soundPlaying && playerIsAlive) {
                    playBounceSound()
                }
                if (player.y < shape.y) {
                    return 1
                } else
                    return -1
            }
        }
        return 0
    }
}