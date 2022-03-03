package com.uwallet.pay.core.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 李明
 * @Date: 2019/9/3 13:58
 * @Description: Excel解析工具
 */

public class POIUtils {

    private final static String XLS = "xls";
    private final static String XLSX = "xlsx";
    private final static String ERROR_MESSAGE = "当前 %s 表格的第 %s 行的第 %s 列的数据类型不匹配";

    /**
     * 解析 excel 文件
     *
     * @param formFile
     * @return Excel 表中数据
     * @throws IOException
     */
    public static List<String[]> readExcel(MultipartFile formFile) throws IOException {
        //检查文件
        checkFile(formFile);
        //获得工作簿对象
        Workbook workbook = getWorkBook(formFile);
        //创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        List<String[]> list = new ArrayList<>();
        if (null != workbook) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                //获取当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (null == sheet) {
                    continue;
                }
                //获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                //循环除了第一行之外的所有行
                for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
                    //获得当前行
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    //循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
        }
        return list;
    }

    /**
     * 基于反射解析 Excel 文件
     * @param multipartFile
     * @param clazz 被解析类的 Class 对象
     * @param <T>
     * @return 返回泛型类的数据集
     * @throws Exception 若表中某个单元格值的类型不符合泛型类属性的数据类型，则抛出 Exception 异常
     */
    public static <T> List<T> readExcel(MultipartFile multipartFile, Class clazz) throws Exception {
        // 检查文件
        checkFile(multipartFile);
        // 获得工作簿对象
        Workbook workbook = getWorkBook(multipartFile);
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        List<T> dataList = new ArrayList<T>();
        // 创建泛型对象
        T datum;
        Map<Integer, Field> map = null;
        Field[] fields = clazz.getDeclaredFields();
        if (workbook != null) {
            // 遍历工作表
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                // 获取当前工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }
                // 获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                // 获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                // 循环除了第一行之外的所有行
                for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
                    // 获得当前行
                    Row row = sheet.getRow(rowNum);
                    // 获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    // 获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    // 遍历表头信息
                    if (rowNum == firstRowNum) {
                        String[] strings = new String[lastCellNum];
                        for (int cellNum = firstRowNum; cellNum < lastCellNum; cellNum++){
                            Cell cell = row.getCell(cellNum);
                            strings[cellNum] = getCellValue(cell);
                        }
                        map = getFieldMap(strings, fields);
                        continue;
                    }
                    if (row == null) {
                        continue;
                    }
                    datum = (T) clazz.newInstance();
                    // 循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        if (cell == null) {
                            continue;
                        }
                        Object value = null;
                        Class type = map.get(cellNum).getType();
                        // 判断当前字段的数据类型(这里可以根据项目需求判断不同的数据类型)
                        if (type.equals(String.class)) {
                            value = getCellValue(cell);
                        } else if (type.equals(Integer.class)) {
                            int cellType = cell.getCellType();
                            if (cellType != Cell.CELL_TYPE_NUMERIC) {
                                throw new Exception(String.format(ERROR_MESSAGE, sheet.getSheetName(), rowNum + 1, cellNum + 1));
                            }
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            value = Integer.parseInt(getCellValue(cell));
                        } else if (type.equals(java.util.Date.class)) {
                            try {
                                value = cell.getDateCellValue();
                            } catch (Exception e) {
                                throw new Exception(String.format(ERROR_MESSAGE, sheet.getSheetName(), rowNum + 1, cellNum + 1));
                            }
                        }
                        PropertyUtils.setProperty(datum, map.get(cellNum).getName(), value);
                    }
                    dataList.add(datum);
                }
            }
        }
        return  dataList;
    }

    /**
     * 获得工作簿对象
     *
     * @param formFile
     * @return
     */
    public static Workbook getWorkBook(MultipartFile formFile) {
        //获得文件名
        String fileName = formFile.getOriginalFilename();
        //创建Workbook工作簿对象，表示整个excel
        Workbook workbook = null;
        try {
            //获得excel文件的io流
            InputStream is = formFile.getInputStream();
            //根据文件后缀名不同（xls和xlsx）获得不同的workbook实现类对象
            if (fileName.endsWith(XLS)) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(XLSX)) {
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    /**
     * 检查文件 
     *
     * @param formFile
     * @throws IOException
     */
    public static void checkFile(MultipartFile formFile) throws IOException {
        //判断文件是否存在
        if (null == formFile) {
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = formFile.getOriginalFilename();
        //判断文件是否是excel文件
        if (!fileName.endsWith(XLS) && !fileName.endsWith(XLSX)) {
            throw new IOException(fileName + "不是excel文件！");
        }
    }

    /**
     * 基于反射生成 Excel 文件，如果 list 为 null，则生成 Excel 模版文件
     * @param list 数据集
     * @param clazz 数据集类的 Class 对象
     * @param fileName 生成的 Excel 文件的文件名，如没有指定，则默认生成文件名为：Excel.xls
     * @param url 存放生成的 Excel 文件的路径
     * @param <T>
     * @throws Exception
     */
    public static<T> void createExcel(List<T> list, Class clazz, String fileName, String url) throws Exception {
        fileName = getFileName(fileName);
        // 创建 Workbook 对象(excel 的文档对象)
        Workbook workbook = getWorkbook(fileName);
        // 建立新的 sheet 对象(excel 的表单)
        Sheet sheet = workbook.createSheet("sheet1");
        // 在 sheet 里创建第一行，参数为行索引(excel 的行)，可以是 0-65535 之间的任何一个数字
        Row row = sheet.createRow(0);
        // 表头
        // 通过反射获得对象的所有字段信息
        Field[] fields = clazz.getDeclaredFields();
        for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
            row.createCell(fieldIndex).setCellValue(fields[fieldIndex].getName());
        }

        if (list != null) {
            // 插入数据
            Row dataRow = null;
            Field field = null;
            for (int listIndex = 0; listIndex < list.size(); listIndex++) {
                T datum = list.get(listIndex);
                dataRow = sheet.createRow(listIndex + 1);
                for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                    field = fields[fieldIndex];
                    // 获取单元格
                    Cell cell = dataRow.createCell(fieldIndex);
                    // 获取单个字段属性
                    Object property = PropertyUtils.getProperty(datum, field.getName());
                    // 判断字段的数据类型
                    if (Date.class.equals(field.getType())) {
                        cell.setCellValue((Date) property);
                    } else if (String.class.equals(field.getType())) {
                        cell.setCellValue((String) property);
                    } else if (Integer.class.equals(field.getType())) {
                        cell.setCellValue((Integer) property);
                    }
                }
            }
        }

        // 写入本地
        writeExcel(workbook, url, fileName);

    }

    /**
     * 通过表头获取 field 的 map 集合
     * @param strings
     * @param fields
     * @return
     */
    private static Map<Integer, Field> getFieldMap(String[] strings, Field[] fields) {
        Map<Integer, Field> map = new HashMap<Integer, Field>(strings.length);
        for (int stringsIndex = 0; stringsIndex < strings.length; stringsIndex++) {
            for (Field field : fields) {
                if (strings[stringsIndex].equals(field.getName())) {
                    map.put(stringsIndex, field);
                }
            }
        }
        return map;
    }

    /**
     * 获取当前行数据
     *
     * @param cell
     * @return
     */
    @SuppressWarnings("deprecation")
    private static String getCellValue(Cell cell) {
        String cellValue = "";

        if (cell == null) {
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()) {
            //数字
            case Cell.CELL_TYPE_NUMERIC:
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            //字符串
            case Cell.CELL_TYPE_STRING:
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            //Boolean
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            //公式
            case Cell.CELL_TYPE_FORMULA:
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            //空值
            case Cell.CELL_TYPE_BLANK:
                cellValue = "";
                break;
            //故障
            case Cell.CELL_TYPE_ERROR:
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    /**
     * 获取 Excel 的文件名
     *
     * @param fileName
     * @return
     */
    private static String getFileName(String fileName) {
        if ("".equals(fileName) || fileName == null) {
            fileName = "Excel.xls";
        } else if (!fileName.endsWith(XLS) && !fileName.endsWith(XLSX)) {
            fileName = fileName + ".xls";
        }
        return fileName;
    }

    /**
     * 通过 fileName 获取 workbook 对象
     * @param fileName
     * @return
     */
    private static Workbook getWorkbook(String fileName) {
        Workbook workbook = null;
        if (fileName.endsWith(XLS)) {
            //2003
            workbook = new HSSFWorkbook();
        } else if (fileName.endsWith(XLSX)) {
            //2007
            workbook = new XSSFWorkbook();
        }
        return workbook;
    }

    /**
     * 将 Excel 写入本地
     *
     * @param workbook
     * @param url
     * @throws IOException
     */
    private static void writeExcel(Workbook workbook, String url, String fileName) throws IOException {
        // 创建路径
        File file = new File(url + "/" + fileName);
        // 判断路径是否存在
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        // 将 Excel 文件写入本地
        workbook.write(outputStream);
        outputStream.close();
    }
}
