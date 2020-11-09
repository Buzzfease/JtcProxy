package config

import com.google.gson.Gson
import utils.CommonUtil

object Config {
    var proxyOpen:Boolean = false
    var isLimit24:Boolean = false

    fun getAppConfig():AppConfig{
        val jsonStr = CommonUtil.readJsonFile("AppConfig.json")
        return if (jsonStr == null){
            AppConfig()
        }else{
            Gson().fromJson(jsonStr, AppConfig::class.java)
        }
    }

    fun saveAppConfig(config: AppConfig){
        val jsonStr = Gson().toJson(config)
        CommonUtil.saveDataToFile(jsonStr, "AppConfig.json")
    }
}