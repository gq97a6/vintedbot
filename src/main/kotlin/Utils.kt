import classes.Product
import classes.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.io.Buffer
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

fun ByteArray.toOkioSource() = Buffer().apply { write(this@toOkioSource) }

suspend fun downloadImage(imageUrl: String): Buffer? {
    val client = HttpClient(CIO)

    return try {
        client.get(imageUrl).readRawBytes().toOkioSource()
    } catch (e: Exception) {
        println("IMAGE_DOWNLOAD_ERROR: ${e.message}")
        null
    } finally {
        client.close()
    }
}

suspend fun parseProducts(json: String): List<Product> {
    val products = mutableListOf<Product>()
    val itemsNode = mapper.readTree(json).get("items")

    try {
        for (itemNode in itemsNode) {
            val id = itemNode.get("id").asText()
            val path = itemNode.get("path").asText()
            val title = itemNode.get("title").asText()
            val price = itemNode.get("price").get("amount").asText().toFloatOrNull() ?: 0f
            val currencyCode = itemNode.get("price").get("currency_code").asText()
            val thumbnailUrl = itemNode.get("photo").get("thumbnails")[2].get("url").asText()
            val thumbnail = downloadImage(thumbnailUrl) ?: Buffer()

            val userNode = itemNode.get("user")
            val userId = userNode.get("id").asText()
            val userLogin = userNode.get("login").asText()
            val userProfileUrl = userNode.get("profile_url").asText()

            val user = User(userId, userLogin, userProfileUrl)
            val product = Product(id, path, user, title, price, currencyCode, thumbnail)

            products.add(product)
        }
    } catch (e: Exception) {
        println("PRODUCT_PARSING_ERROR: ${e.message}")
    }

    return products
}

fun Product.toChatMessage(): String {
    var log = "**${this.title}** [${this.user.login}]\n"
    log += "**PRICE:** ${this.price} ${this.currencyCode}\n"
    log += "<" + "${Config.baseUrl}${this.path}" + ">"

    return log
}

// Run specific action in a loop with a driver that is refreshed every X iterations
suspend fun webDriverLoop(refreshAfter: Int = 10, action: suspend WebDriver.() -> Unit) {
    var counter = 0
    var driver: WebDriver? = null

    try {
        while (true) {
            // Create or refresh driver every 10 iterations
            if (driver == null || counter >= refreshAfter) {
                driver?.quit()
                //driver = RemoteWebDriver(URI(gridUrl).toURL(), ChromeOptions())
                driver = ChromeDriver(Config.chromeOptions)
                counter = 0
            }

            try {
                driver.action()
                counter++
            } catch (e: Exception) {
                println("WEB_DRIVER_LOOP_ERROR: ${e.message}")
                driver.quit()
                driver = null
            }
        }
    } finally {
        println("WEB_DRIVER_QUIT")
        driver?.quit()
    }
}