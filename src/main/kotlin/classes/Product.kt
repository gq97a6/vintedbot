package classes

import kotlinx.io.Buffer

class Product(
    val id: String = "invalid",
    val path: String = "invalid",
    val user: User = User(),
    val title: String = "invalid",
    val price: Float = 0f,
    val currencyCode: String = "invalid",
    val thumbnail: Buffer = Buffer()
)