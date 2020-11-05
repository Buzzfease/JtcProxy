data class ProxyResult(
    val code: Int,
    val `data`: List<ProxyItem>,
    val msg: String,
    val success: Boolean
)

data class ProxyItem(
    val ip: String,
    val port: Int
)