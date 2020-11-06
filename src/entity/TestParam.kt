package entity
/**
 * created by Buzz
 * on 2020/11/4
 * email lmx2060918@126.com
 */
data class TestParam(
    var applictionType: String = "APP",
    var applictionVersion: String = "40203",
    var carNo: String = "陕-A123456",
    var distance: String = "3000",
    var isNewReport: Int = 1,
    var latitude: Double = 34.21485,
    var longitude: Double = 108.891383,
    var nonce: String = "1604459579701-1986972922",
    var privacyOption: Int = 1,
    var sign: String = "CB6F7336A1229C4A5A37864DACB6B7E5",
    var signType: String = "MD5",
    var timestamp: String = "1604459579701",
    var userId: String = "49cc2e8926d84a2ba8532af00e2f846d"
)