package network

import config.Config
import entity.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    private const val DEFAULT_TIMEOUT = 10

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
                .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("proxy.wandouip.com", 8090)))
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
        fun requestSuccess(t: T, successCount:Int, failedCount:Int)
        fun requestFail(message: String?)
        fun requestOnGoing(count:Int)
    }

    fun querySingle(carNo:String, callBack: BaseCallBack<InfoResult>){
        querySingleFormServer(carNo, callBack)
    }

    fun queryMany(carNoList:ArrayList<String>, callBack: BaseCallBack<ArrayList<InfoResult>>){
        queryLoopFormServer(carNoList, callBack)
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
                        if (response.body()!!.obj.retcode == 0){
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
                                    callBack.requestSuccess(response.body()!!,0,0)
                                }
                                override fun onFailure(call: Call<InfoResult>, t: Throwable) {
                                    val constResult = InfoResult()
                                    constResult.constCarNo = infoParam.carNo
                                    callBack.requestSuccess(constResult,0,0)
                                }
                            })
                        }else{
                            val constResult = InfoResult()
                            constResult.constCarNo = testParam.carNo
                            callBack.requestSuccess(constResult,0,0)
                        }
                    }
                    else ->{
                        callBack.requestFail("Ip被封禁")
                    }
                }
            }

            override fun onFailure(call: Call<CarResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    private fun queryLoopFormServer(carNoList:ArrayList<String>, callBack: BaseCallBack<ArrayList<InfoResult>>){
        val resultList = ArrayList<InfoResult>()
        val total = carNoList.size
        var current = 0
        var successCount = 0
        var failedCount = 0
        var qms = Config.getAppConfig().queryTime

        run loop@{
            carNoList.forEach {
                val testParam = QureyParam()
                testParam.carNo = it
                val api: RespApi = provideMainRetrofit().create(RespApi::class.java)
                val mCall: Call<CarResult> = api.getCarInfo(testParam)
                mCall.enqueue(object : Callback<CarResult> {
                    override fun onResponse(call: Call<CarResult>, response: Response<CarResult>) {
                        when(response.body()!!.resultCode){
                            0 ->{
                                if (response.body()!!.obj.retcode == 0){
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
                                            callBack.requestOnGoing(current)
                                            when(response.body()!!.resultCode){
                                                0 ->{
                                                    resultList.add(response.body()!!)
                                                }
                                                else ->{
                                                    val constResult = InfoResult()
                                                    constResult.constCarNo = infoParam.carNo
                                                    resultList.add(constResult)
                                                }
                                            }
                                            if (current == total){
                                                callBack.requestSuccess(resultList, successCount, failedCount)
                                            }
                                        }
                                        override fun onFailure(call: Call<InfoResult>, t: Throwable) {
                                            current++
                                            failedCount++
                                            callBack.requestOnGoing(current)
                                            if (current == total){
                                                callBack.requestSuccess(resultList, successCount, failedCount)
                                            }
                                        }
                                    })
                                }else{
                                    current++
                                    successCount++
                                    callBack.requestOnGoing(current)
                                    val constResult = InfoResult()
                                    constResult.constCarNo = it
                                    resultList.add(constResult)
                                    if (current == total){
                                        callBack.requestSuccess(resultList, successCount, failedCount)
                                    }
                                }
                            }
                            else ->{
                                //ip被封禁
                                current++
                                failedCount++
                                callBack.requestOnGoing(current)
                                if (current == total){
                                    callBack.requestSuccess(resultList, successCount, failedCount)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<CarResult>, t: Throwable) {
                        //代理出问题或网络不通
                        current++
                        failedCount++
                        callBack.requestOnGoing(current)
                        if (current == total){
                            callBack.requestSuccess(resultList, successCount, failedCount)
                        }
                    }
                })
                Thread.sleep(qms)
            }
        }
    }

    fun getWhiteList(callBack: BaseCallBack<WhiteListResult>){
        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val call: Call<WhiteListResult> = api.getWhiteList(Config.getAppConfig().appKey)
        call.enqueue(object : Callback<WhiteListResult>{
            override fun onResponse(call: Call<WhiteListResult>, response: Response<WhiteListResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!,0,0)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }

            }
            override fun onFailure(call: Call<WhiteListResult>, t: Throwable) {
                callBack.requestFail(t.message)
            }
        })
    }

    fun addWhiteList(id:String?, callBack: BaseCallBack<WhiteListResult>){
        val api: RespApi = provideSettingRetrofit().create(RespApi::class.java)
        val paramMap = HashMap<String?,String?>()
        if (id != null){
            paramMap["id"] = id
        }
        paramMap["app_key"] = Config.getAppConfig().appKey
        paramMap["ip"] = Config.getAppConfig().ip

        val call: Call<WhiteListResult> = api.updateWhiteList(paramMap)
        call.enqueue(object : Callback<WhiteListResult>{
            override fun onResponse(call: Call<WhiteListResult>, response: Response<WhiteListResult>) {
                if (response.body()!!.code == 200){
                    callBack.requestSuccess(response.body()!!,0,0)
                }else{
                    callBack.requestFail(response.body()!!.msg)
                }
            }
            override fun onFailure(call: Call<WhiteListResult>, t: Throwable) {
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
    }
}