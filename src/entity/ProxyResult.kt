package entity

data class ProxyResult(
    val code: Int,
    val `data`: List<IpItem>,
    val msg: String
)

data class IpItem(
    val ip: String,
    val port: Int
)