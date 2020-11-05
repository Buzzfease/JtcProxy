import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
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
    var isLoopQuerying = false


    private fun provideMainRetrofit():Retrofit{
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://sytgate.jslife.com.cn/core-gateway/")
                .client(provideTrustAllClient())
                .build()
    }


    private fun provideTrustAllClient(): OkHttpClient {
        val trustAllCerts = buildTrustManagers()
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder:OkHttpClient.Builder = OkHttpClient.Builder()

        if (Config.proxyOpen){
            return builder.readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, trustAllCerts!![0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true  }
                    .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .addInterceptor(jsonHeaderInterceptor())
                    .addInterceptor(LogInterceptor())
                    .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("proxy.wandouip.com", 8090)))
                    .retryOnConnectionFailure(true).build()

        }else{
            return builder.readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, trustAllCerts!![0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true  }
                    .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .addInterceptor(jsonHeaderInterceptor())
                    .addInterceptor(LogInterceptor())
                    .retryOnConnectionFailure(true).build()
        }
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
        fun requestSuccess(t: T, times:Int, successCount:Int, failedCount:Int)
        fun requestFail(message: String?)
    }

    interface RespApi {
        @POST("order/carno/pay/appindex")
        fun getCarInfo(@Body param: TestParam): Call<CarResult>
    }

    fun querySingle(carNo:String, callBack: BaseCallBack<CarResult>){
        querySingleFormServer(carNo, callBack)
    }

    fun queryLoop(carNoList:ArrayList<String>, callBack: BaseCallBack<ArrayList<CarResult>>){
        Thread(QueryThread(carNoList,callBack)).start()
    }

    private fun querySingleFormServer(carNo:String, callBack: BaseCallBack<CarResult>){
        val testParam = TestParam()
        testParam.carNo = carNo

        val api: RespApi = provideMainRetrofit().create(RespApi::class.java)
        val mCall: Call<CarResult> = api.getCarInfo(testParam)
        mCall.enqueue(object : Callback<CarResult> {
            override fun onResponse(call: Call<CarResult>, response: Response<CarResult>) {
                if (null == response.body()) {
                    return
                }
                when(response.body()!!.resultCode){
                    0 ->{
                        callBack.requestSuccess(response.body()!!,0,0,0)
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

    fun queryLoopFormServer(carNoList:ArrayList<String>, callBack: BaseCallBack<ArrayList<CarResult>>, requestTimes:Int){
        val resultList = ArrayList<CarResult>()
        val total = carNoList.size
        var current = 0
        var successCount = 0
        var failedCount = 0
        carNoList.forEach {
            val testParam = TestParam()
            testParam.carNo = it
            val api: RespApi = provideMainRetrofit().create(RespApi::class.java)
            val mCall: Call<CarResult> = api.getCarInfo(testParam)
            mCall.enqueue(object : Callback<CarResult> {
                override fun onResponse(call: Call<CarResult>, response: Response<CarResult>) {
                    current++
                    if (null == response.body()) {
                        failedCount++
                        return
                    }
                    when(response.body()!!.resultCode){
                        0 ->{
                            successCount++
                            resultList.add(response.body()!!)
                        }
                        else ->{
                            failedCount++
                        }
                    }
                    if (current == total){
                        callBack.requestSuccess(resultList, requestTimes, successCount, failedCount)
                    }
                }

                override fun onFailure(call: Call<CarResult>, t: Throwable) {
                    current++
                    failedCount++
                    if (current == total){
                        callBack.requestSuccess(resultList, requestTimes, successCount, failedCount)
                    }
                }
            })
        }
    }

    class QueryThread(var carNoList:ArrayList<String>, var callBack: BaseCallBack<ArrayList<CarResult>>) :Runnable{
        var requestTimes:Int = 0
        override fun run() {
            if (Config.isLoop){
                while (isLoopQuerying){
                    requestTimes++
                    queryLoopFormServer(carNoList, callBack, requestTimes)
                    Thread.sleep(5000)
                }
            }
        }
    }
}