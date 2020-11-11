package network

import config.Config
import entity.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object Network {

    private var proxyList = ArrayList<IpItem>()
    private var requestTimes = 0
    private var changeIpCount = 15
    private var isProxyOpen:Boolean = false
    private const val DEFAULT_TIMEOUT = 10
    var isQuerying = false

    private fun provideMainRetrofit():Retrofit{
        return if (Config.proxyOpen){
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://sytgate.jslife.com.cn/core-gateway/")
                    .client(provideProxyClient())
                    .build()
        }else{
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://sytgate.jslife.com.cn/core-gateway/")
                    .client(provideNormalClient())
                    .build()
        }
    }

    private fun provideInfoRetrofit():Retrofit{
        return if (Config.proxyOpen){
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://jparking.jslife.com.cn/jparking-service/")
                    .client(provideProxyClient())
                    .build()
        }else{
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://jparking.jslife.com.cn/jparking-service/")
                    .client(provideNormalClient())
                    .build()
        }

    }

    private fun provideSettingRetrofit():Retrofit{
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://api.wandoudl.com/")
                .client(provideNormalClient())
                .build()
    }

    private fun provideNormalClient(): OkHttpClient{
        val trustAllCerts = buildTrustManagers()
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder:OkHttpClient.Builder = OkHttpClient.Builder()

        return builder.readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, trustAllCerts!![0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true  }
                .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(jsonHeaderInterceptor())
                .addInterceptor(LogInterceptor())
                .retryOnConnectionFailure(true).build()
    }

    private fun provideProxyClient(): OkHttpClient{
        requestTimes++
        val index = if (requestTimes / changeIpCount > proxyList.size - 1){
            proxyList.size -1
        }else{
            requestTimes / changeIpCount
        }
        val trustAllCerts = buildTrustManagers()
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder:OkHttpClient.Builder = OkHttpClient.Builder()

        return builder.readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, trustAllCerts!![0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true  }
                .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(jsonHeaderInterceptor())
                .addInterceptor(LogInterceptor())
                .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyList[index].ip, proxyList[index].port)))
                .proxyAuthenticator { route, response ->
                    return@proxyAuthenticator response.request().newBuilder().build()
                }
                .retryOnConnectionFailure(true).build()
    }


    private fun jsonHeaderInterceptor(): Interceptor {
        return Interceptor {
            chain: Interceptor.Chain -> chain.proceed(chain.request().newBuilder()
                .addHeader("Content-Type","application/json")
                .build())
        }
    }

    private fun buildTrustManagers(): Array<TrustManager>? {
        return arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
        )
    }

    /**
     * 网络请求数据返回基类
     */
    interface BaseCallBack<T> {
        fun requestSuccess(t: T, current:Int, successCount:Int, failedCount:Int)
        fun requestFail(message: String?)
        fun requestOnGoing(current:Int, successCount:Int, failedCount:Int)
        fun requestProxyError(message: String?)
        fun requestComplete(current:Int, successCount:Int, failedCount:Int)
    }

    interface ProxyCallBack<T> {
        fun requestSuccess(t: T)
        fun requestFail(message: String?)
    }

    interface WhiteListCallBack<T> {
        fun requestSuccess(t: T)
        fun requestFail(message: String?)
    }

    interface CheckFeeCallBack<T> {
        fun requestSuccess(t: T)
        fun requestFail(message: String?)
    }

    fun querySingle(carNo:String, callBack: BaseCallBack<InfoResult>){
        isProxyOpen = Config.proxyOpen
        if (isProxyOpen){
            requestTimes = 0
            changeIpCount = Config.getAppConfig().changeIpCount
            getProxyIp(1, object :ProxyCallBack<ProxyResult>{
                override fun requestSuccess(t: ProxyResult) {
                    when(t.code){
                        200 ->{
                            proxyList = t.data as ArrayList<IpItem>
                            querySingleFormServer(carNo, callBack)
                        }
                        40000 ->{ callBack.requestProxyError("认证参数错误(系统错误，请联系豌豆客服)")}
                        40002 ->{ callBack.requestProxyError("ip不在白名单(请打开设置配置)")}
                        40004 ->{ callBack.requestProxyError("ip认证失败/ip白名单过期(请打开设置配置)")}
                        40006 ->{ callBack.requestProxyError("认证关联的用户异常/用户不存在(ip白名单错误/认证数据错误)")}
                        40008 ->{ callBack.requestProxyError("代理节点不存在(代理ip过期导致，请重新查询)")}
                        40010 ->{ callBack.requestProxyError("套餐已经使用完(打开豌豆代理官网充值套餐即可)")}
                    }
                }

                override fun requestFail(message: String?) {
                    callBack.requestProxyError(message)
                }
            })
        }else{
            querySingleFormServer(carNo, callBack)
        }
    }

    fun queryMany(carNoList:ArrayList<String>, callBack: BaseCallBack<InfoResult>){
        isProxyOpen = Config.proxyOpen
        if (isProxyOpen){
            requestTimes = 0
            changeIpCount = Config.getAppConfig().changeIpCount
            val requestProxyNum:Int = (carNoList.size / changeIpCount) + 1
            getProxyIp(requestProxyNum, object :ProxyCallBack<ProxyResult>{
                override fun requestSuccess(t: ProxyResult) {
                    when(t.code){
                        200 ->{
                            proxyList = t.data as ArrayList<IpItem>
                            queryLoopFormServer(carNoList, callBack)
                        }
                        40000 ->{ callBack.requestProxyError("认证参数错误(系统错误，请联系豌豆客服)")}
                        40002 ->{ callBack.requestProxyError("ip不在白名单(请打开设置配置)")}
                        40004 ->{ callBack.requestProxyError("ip认证失败/ip白名单过期(请打开设置配置)")}
                        40006 ->{ callBack.requestProxyError("认证关联的用户异常/用户不存在(ip白名单错误/认证数据错误)")}
                        40008 ->{ callBack.requestProxyError("代理节点不存在(代理ip过期导致，请重新查询)")}
                        40010 ->{ callBack.requestProxyError("套餐已经使用完(打开豌豆代理官网充值套餐即可)")}
                    }
                }

                override fun requestFail(message: String?) {
                    callBack.requestProxyError(message)
                }
            })
        }else{
            queryLoopFormServer(carNoList, callBack)
        }
    }


    private fun querySingleFormServer(carNo:String, callBack: BaseCallBack<InfoResult>){
        val testParam = QureyParam()
        testParam.carNo = carNo

        val api: RespApi = provideMainRetrofit().create(RespApi::class.java)
        val mCall: Call<CarResult> = api.getCarInfo(testParam)
        mCall.enqueue(object : Callback<CarResult> {
            override fun onResponse(call: Call<CarResult>, response: Response<CarResult>) {
                when(response.body()!!.resultCode){
                    0 ->{
                        if (response.body()!!.message == "无法找到场内车"){
                            val constResult = InfoResult()
                            constResult.constCarNo = testParam.carNo
                            callBack.requestSuccess(constResult,0, 0,0)
                        }else{
                            //真实存在
                            val infoParam = InfoParam()
                            infoParam.carNo = response.body()!!.obj.carNo
                            infoParam.orderType = response.body()!!.obj.orderType
                            infoParam.parkCode = response.body()!!.obj.parkCode
                            infoParam.userId = testParam.userId

                            val api2: RespApi = provideInfoRetrofit().create(RespApi::class.java)
                            val mCall2: Call<InfoResult> = api2.getCarRealInfo(infoParam)
                            mCall2.enqueue(object : Callback<InfoResult>{
                                override fun onResponse(call: Call<InfoResult>, response: Response<InfoResult>) {
                                    callBack.requestSuccess(response.body()!!,0,0,0)
                                }
                                override fun onFailure(call: Call<InfoResult>, t: Throwable) {
                                    val constResult = InfoResult()
                                    constResult.constCarNo = infoParam.carNo
                                    callBack.requestSuccess(constResult,0,0,0)
                                }
                            })
                        }
                    }
                    else ->{
                        callBack.requestFail("Ip被封禁")
                    }
                }
            }

            override fun onFailure(call: Call<CarResult>, t: Throwable) {
                if (isProxyOpen){
                    callBack.requestProxyError("代理失败，请检查ip白名单和套餐余额")
                }else{
                    callBack.requestProxyError(t.message)
                }
            }
        })
    }

    private fun queryLoopFormServer(carNoList:ArrayList<String>, callBack: BaseCallBack<InfoResult>){
        val total = carNoList.size
        var current = 0
        var successCount = 0
        var failedCount = 0
        val qms = Config.getAppConfig().queryTime

        run loop@{
            carNoList.forEach {
                if (isQuerying){
                    val testParam = QureyParam()
                    testParam.carNo = it
                    val api: RespApi = provideMainRetrofit().create(RespApi::class.java)
                    val mCall: Call<CarResult> = api.getCarInfo(testParam)
                    mCall.enqueue(object : Callback<CarResult> {
                        override fun onResponse(call: Call<CarResult>, response: Response<CarResult>) {
                            when(response.body()!!.resultCode){
                                0 ->{
                                    if (response.body()!!.message == "无法找到场内车"){
                                        current++
                                        successCount++
                                        val constResult = InfoResult()
                                        constResult.constCarNo = it
                                        callBack.requestSuccess(constResult, current, successCount, failedCount)
                                        if (current == total){
                                            callBack.requestComplete(current, successCount, failedCount)
                                        }
                                    }else{
                                        //真实存在
                                        val infoParam = InfoParam()
                                        infoParam.carNo = response.body()!!.obj.carNo
                                        infoParam.orderType = response.body()!!.obj.orderType
                                        infoParam.parkCode = response.body()!!.obj.parkCode
                                        infoParam.userId = testParam.userId

                                        val api2: RespApi = provideInfoRetrofit().create(RespApi::class.java)
                                        val mCall2: Call<InfoResult> = api2.getCarRealInfo(infoParam)
                                        mCall2.enqueue(object : Callback<InfoResult>{
                                            override fun onResponse(call: Call<InfoResult>, response: Response<InfoResult>) {
                                                current++
                                                successCount++
                                                var constResult:InfoResult? = null
                                                when(response.body()!!.resultCode){
                                                    0 ->{
                                                        constResult = response.body()!!
                                                    }
                                                    else ->{
                                                        constResult = InfoResult()
                                                        constResult.constCarNo = infoParam.carNo
                                                    }
                                                }
                                                callBack.requestSuccess(constResult, current, successCount, failedCount)
                                                if (current == total){
                                                    callBack.requestComplete(current, successCount, failedCount)
                                                }
                                            }
                                            override fun onFailure(call: Call<InfoResult>, t: Throwable) {
                                                current++
                                                failedCount++
                                                callBack.requestOnGoing(current, successCount, failedCount)
                                                if (current == total){
                                                    callBack.requestComplete(current, successCount, failedCount)
                                                }
                                            }
                                        })
                                    }
                                }
                                else ->{
                                    //ip被封禁
                                    current++
                                    failedCount++
                                    callBack.requestOnGoing(current, successCount, failedCount)
                                    if (current == total){
                                        callBack.requestComplete(current, successCount, failedCount)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<CarResult>, t: Throwable) {
                            //代理出问题或网络不通
                            current++
                            failedCount++
                            callBack.requestOnGoing(current, successCount, failedCount)
                            if (current == total){
                                callBack.requestComplete(current, successCount, failedCount)
                            }
                        }
                    })
                    Thread.sleep(qms)
                }
            }
        }
    }

    fun getWhiteList(callBack: WhiteListCallBack<WhiteListResult>){
        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val call: Call<WhiteListResult> = api.getWhiteList(Config.getAppConfig().appKey)
        call.enqueue(object : Callback<WhiteListResult>{
            override fun onResponse(call: Call<WhiteListResult>, response: Response<WhiteListResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }

            }
            override fun onFailure(call: Call<WhiteListResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    fun addWhiteList(id:String?, callBack: WhiteListCallBack<WhiteListResult>){
        val paramMap = HashMap<String?,String?>()
        if (id != null){
            paramMap["id"] = id
        }
        paramMap["app_key"] = Config.getAppConfig().appKey
        paramMap["ip"] = Config.getAppConfig().ip

        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val call: Call<WhiteListResult> = api.updateWhiteList(paramMap)
        call.enqueue(object : Callback<WhiteListResult>{
            override fun onResponse(call: Call<WhiteListResult>, response: Response<WhiteListResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }
            }
            override fun onFailure(call: Call<WhiteListResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    private fun getProxyIp(amount:Int?,callBack: ProxyCallBack<ProxyResult>){
        val paramMap = HashMap<String?,String?>()
        paramMap["app_key"] = Config.getAppConfig().appKey
        paramMap["num"] = amount.toString()
        paramMap["port"] = "1"
        paramMap["xy"] = "1"
        paramMap["mr"] = "1"

        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val call: Call<ProxyResult> = api.getProxyIp(paramMap)
        call.enqueue(object : Callback<ProxyResult>{
            override fun onResponse(call: Call<ProxyResult>, response: Response<ProxyResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }
            }
            override fun onFailure(call: Call<ProxyResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    fun checkFee(appKey:String?, callBack: CheckFeeCallBack<CheckFeeResult>){
        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val call: Call<CheckFeeResult> = api.checkFee(appKey)
        call.enqueue(object : Callback<CheckFeeResult>{
            override fun onResponse(call: Call<CheckFeeResult>, response: Response<CheckFeeResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }
            }
            override fun onFailure(call: Call<CheckFeeResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    interface RespApi {
        @POST("order/carno/pay/appindex")
        fun getCarInfo(@Body param: QureyParam): Call<CarResult>

        @POST("order/carno/pay")
        fun getCarRealInfo(@Body param: InfoParam): Call<InfoResult>

        @GET("api/whitelist/list")
        fun getWhiteList(@Query("app_key") key:String?):Call<WhiteListResult>

        //修改/更新ip白名单，如果只传ip且ip未达到总使用量则会新增，否则请指定id值，更新对应id的白名单ip
        @GET("api/whitelist/update")
        fun updateWhiteList(@QueryMap param:HashMap<String?, String?>):Call<WhiteListResult>

        @GET("api/ip")
        fun getProxyIp(@QueryMap param:HashMap<String?, String?>):Call<ProxyResult>

        @GET("api/product/list")
        fun checkFee(@Query("app_key") key:String?):Call<CheckFeeResult>
    }
}