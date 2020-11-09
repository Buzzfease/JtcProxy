package entity

data class CheckFeeResult(
    val code: Int,
    val `data`: List<FeeDetail>,
    val msg: String
)

data class FeeDetail(
    val expire_time: String,
    val id: String,
    val is_available: Boolean,
    val product_limit: String,
    val product_name: String,
    val remain: String,
    val remain_connect: Int,
    val remain_connect2: Int,
    val remain_extract: Int,
    val remain_extract2: Int,
    val time: String
)