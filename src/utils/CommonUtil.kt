package utils

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat


object CommonUtil {
    fun handleCarNo(input: String): String {
        val str = input.trim()
        if (str.isEmpty() || str.length < 6) {
            return ""
        }
        val sb = StringBuilder(str)
        if (!str.contains("-")) {
            sb.insert(1, "-")
        }
        return sb.toString()
    }

    //.xlsx
    fun readExcels(path:String):ArrayList<String>{
        val mList = ArrayList<String>()
        val pkg = OPCPackage.open(path)
        val excel = XSSFWorkbook(pkg)
        //获取第一个sheet
        val sheet = excel.getSheetAt(0)
        sheet.forEach { row ->
            row.forEach { cell->
                cell.setCellType(CellType.STRING);
                val value = handleCarNo(cell.stringCellValue)
                if (value.isNotEmpty()){
                    mList.add(value)
                }
            }
        }
        mList.forEach {
            System.out.println(it)
        }

        return mList
    }

    //.xls
    fun readExcel(path:String):ArrayList<String>{
        val mList = ArrayList<String>()
        val input = FileInputStream(path)
        val excel = HSSFWorkbook(input)
        //获取第一个sheet
        val sheet =excel.getSheetAt(0)
        sheet.forEach { row ->
            row.forEach { cell ->
                cell.setCellType(CellType.STRING);
                val value = handleCarNo(cell.stringCellValue)
                if (value.isNotEmpty()){
                    mList.add(value)
                }
            }
        }
        mList.forEach {
            System.out.println(it)
        }
        return mList
    }

    fun parseTime(strTime: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var time = 0L
        try {
            time = format.parse(strTime).time
        } catch (e: ParseException) {
            e.printStackTrace()
            return -1
        }
        return time
    }

    //读取json文件
    fun readJsonFile(fileName: String): String? {
        var jsonStr = ""
        return try {
            val jsonFile = File(fileName)
            val fileReader = FileReader(jsonFile)
            val reader: Reader = InputStreamReader(FileInputStream(jsonFile), "utf-8")
            var ch = 0
            val sb = StringBuffer()
            while (reader.read().also { ch = it } != -1) {
                sb.append(ch.toChar())
            }
            fileReader.close()
            reader.close()
            jsonStr = sb.toString()
            jsonStr
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //写入json文件
    fun saveDataToFile(data: String?, fileName: String) {
        try {
            val jsonFile = File(fileName)
            val writer = FileWriter(jsonFile, false)
            writer.append(data)
            writer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}