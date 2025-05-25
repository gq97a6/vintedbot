import classes.Product
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jessecorbett.diskord.api.channel.FileData
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendFile
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.openqa.selenium.By

val mapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
val savedProducts = mutableListOf<Product>()
val productsChannel = Channel<Product>()

suspend fun main() {
    botJob()
    webDriverLoop(10) {
        get(Config.baseUrl)
        get(Config.requestURL)
        onNewBody(findElement(By.tagName("body")).text)
        delay(30000)
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun botJob() = GlobalScope.launch {
    bot(Config.botToken) {
        events {
            onReady {
                GlobalScope.launch {
                    while (isActive) {
                        val product = productsChannel.receive()
                        delay(100)

                        channel(Config.channelId).sendFile(
                            FileData(product.thumbnail, "img.jpeg"),
                            product.toChatMessage()
                        )
                    }
                }
            }
        }
    }
}

suspend fun onNewBody(body: String) {
    println("NEW_BODY_RECEIVED")
    val newProducts = parseProducts(body).reversed().filter { p -> !savedProducts.any { it.id == p.id } }

    savedProducts.addAll(newProducts)

    (savedProducts.size - 300).let {
        if (it > 300) repeat(it) { savedProducts.removeFirst() }
    }

    newProducts.forEach {
        productsChannel.send(it)
        println(it.toChatMessage())
    }
}