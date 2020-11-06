package utils

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

object CommonUtil {
    fun handleCarNo(input: String): String {
        if (input.isEmpty() || input.length < 5) {
            return ""
        }
        val sb = StringBuilder(input)
        if (!input.contains("-")) {
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

}