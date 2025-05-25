import org.openqa.selenium.chrome.ChromeOptions
import java.lang.System.getenv

object Config {
    val botToken = getenv("BOT_TOKEN") ?: throw IllegalStateException("Environment variable BOT_TOKEN not set")
    val channelId = getenv("CHANNEL_ID") ?: throw IllegalStateException("Environment variable CHANNEL_ID not set")

    val baseUrl = getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL not set")
    private val endpoint = getenv("ENDPOINT") ?: throw IllegalStateException("Environment variable ENDPOINT not set")
    private val params = getenv("PARAMS") ?: throw IllegalStateException("Environment variable PARAMS not set")

    val requestURL = baseUrl + endpoint + params

    // Options to mask that this is automated request
    val chromeOptions = ChromeOptions().apply {
        // Disable automation flags to avoid detection
        addArguments("--disable-blink-features=AutomationControlled")
        addArguments("--disable-infobars")
        addArguments("--no-sandbox")
        addArguments("--headless=new")

        // Set a realistic user agent
        addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")

        // Disable automation extensions and flags
        setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        setExperimentalOption("useAutomationExtension", false)
    }
}