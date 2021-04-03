import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.math.min
import kotlin.random.Random

class Scene2() : Scene() {

    private var jumpIntensity = 4.0
    private var moveIntensity = 2.0
    private var maxSpeed = 8.0
    private var gravity = 0.05

    private var leftWalk = false

    private var mainContainer: Container = Container()
    private var player: Circle = Circle()
    private var groundObjects: MutableList<ShapeView> = mutableListOf()
    private var treasureObjects: MutableList<Image> = mutableListOf()


    override suspend fun Container.sceneInit() {
        mainContainer = this

        // making ground objects
        // size = 512 x 1028
        groundObjects = createGroundObjects()
        treasureObjects = createTreasureObjects()

        initPlayer()

        //walls and player control area
        roundRect(50.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(-40, 0)
        roundRect(50.0, 1080.0, 0.0, 0.0, Colors["#FFFFFF"], Colors["#FFFFFF"], 0.0, true).xy(502, 0)

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
    }

    private fun initPlayer() {

        var horizontal = 1.0 * moveIntensity
        var vertical = 0.0

        player = mainContainer.circle(25.0, Colors.WHITE).xy(256, 700)
        player.onClick {
            player.color = Colors.RED
        }
        player.addUpdater {
            // update ball gravity
            vertical -= gravity

            //bounce on border collission
            if (player.x < 0.0 || player.x > 512 - player.radius * 2) {
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

            // clamp max Ball Speed in all Vertical Direction
            vertical = min(horizontal * vertical, maxSpeed) / horizontal

            // set ball values
            if (leftWalk) {
                player.xy(player.x - horizontal, player.y - vertical)
            } else {
                player.xy(player.x + horizontal, player.y - vertical)
            }

            updateShapePositions()

            if (y > 1080) {
                //handle Death
            }

        }
    }

    private fun activateRandomPowerUp() {
        val powerUpNumber = (Random.nextDouble() * 6).toInt()
        when (powerUpNumber) {
            1 -> gravity -= 0.005
            2 -> gravity += 0.005
            3 -> moveIntensity += 0.5
            4 -> moveIntensity -= 0.5
            5 -> jumpIntensity -= 0.5
            else -> {
                jumpIntensity += 0.5
            }
        }
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
        val ground2 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -200, 650)
        val ground3 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -350, 500)
        val ground4 = mainContainer.roundRect(250.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -250, 300)
        val ground5 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, 400)
        val ground6 = mainContainer.roundRect(150.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -250, 50)
        val ground7 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 450, 150)
        val ground8 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -400, -100)
        val ground9 = mainContainer.roundRect(100.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 512, -250)
        val ground10 = mainContainer.roundRect(200.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 -350, -350)
        val ground11 = mainContainer.roundRect(80.0, 50.0, 0.0, 0.0, Colors.WHITE).xy(512 - 80, -500)
        val ground12 = mainContainer.roundRect(250.0, 50.0, 0.0, 0.0, Colors.YELLOW).xy(512 -512, -650)
        val ground13 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.RED).xy(512 - 50, -850)
        val ground14 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.BLUE).xy(512 - 50, -850)
        val ground15 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.VIOLET).xy(512 - 50, -850)
        val ground16 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GREEN).xy(512 - 50, -850)
        val ground17 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKCYAN).xy(512 - 50, -850)
        val ground18 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.GOLDENROD).xy(512 - 50, -850)
        val ground19 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKGREEN).xy(512 - 50, -850)
        val ground20 = mainContainer.roundRect(50.0, 50.0, 0.0, 0.0, Colors.DARKMAGENTA).xy(512 - 50, -850)

        return mutableListOf(baseground, ground1, ground2,ground3,ground4,ground5, ground6, ground7, ground8,ground9,ground10,ground11,ground12,ground13)
    }

    private suspend fun createTreasureObjects(): MutableList<Image> {
        val bitmap: Bitmap = resourcesVfs["chest_white.png"].readBitmap()
        val image1 = mainContainer.image(bitmap).scale(0.5).position(112, 650)

        return mutableListOf(image1)
    }

    private fun updateShapePositions() {
        if (player.y < 540) {
            val difference = player.y - 540.0
            player.y -= difference
            groundObjects.forEach {
            }
            treasureObjects.forEach {shape ->
                shape.y -= difference
            }
        }
    }

    private fun collidingPowerUps(): Boolean {
        groundObjects.forEach { shape ->
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