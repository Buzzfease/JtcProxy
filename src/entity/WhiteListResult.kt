package entity

data class WhiteListResult(
    val code: Int,
    val `data`: List<IpStatus>,
    val msg: String
)

data class IpStatus(
    val id: String,
    val ip: String,
    val time: String,
    val update_time: String
)