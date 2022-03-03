package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;
import com.uwallet.pay.main.service.CSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Service
@Slf4j
public class CSVServiceImpl implements CSVService {


    /**
     * 生成清算csv文件
     * @param clearList
     * @return
     */
    @Override
    public  void createClearCsvFile(String fileName, String path, List<ClearBillCSVDTO> clearList){
//        if (clearList!=null && clearList.size() > 0){
            log.info("生成清算CSV文件，文件名："+path+fileName) ;
            // 表格头
            String[] headArr = new String[]{"*ContactName","EmailAddress","POAddressLine1","POAddressLine2","POAddressLine3","POAddressLine4","POCity","PORegion","POPostalCode","POCountry","*InvoiceNumber","*InvoiceDate","*DueDate","InventoryItemCode","Description","*Quantity","*UnitAmount","*AccountCode","*TaxType","TrackingName1","TrackingOption1","TrackingName2","TrackingOption2","Currency"};
            //CSV文件路径及名称
            String filePath = path;
            File csvFile = null;
            BufferedWriter csvWriter = null;
            try {
                csvFile = new File(filePath + File.separator + fileName);
                File parent = csvFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                csvFile.createNewFile();

                // GB2312使正确读取分隔符","
                csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "gbk"), 1024);

                // 写入文件头部标题行
                csvWriter.write(String.join(",", headArr));
                csvWriter.newLine();

                // 写入文件内容
                if(clearList != null){
                    for (ClearBillCSVDTO points : clearList) {
                        //todo add by zhangzeyuan 换一种 表头、数据对应方式  类似 k,v 键值对
                        csvWriter.write(points.toRow());
                        csvWriter.newLine();
                    }
                }
                csvWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//        }
    }

    @Override
    public File createCsvFile(String fileName, String path, String[] headArr, List<String> list) {
        String filePath = path;
        File csvFile = null;
        BufferedWriter csvWriter = null;

        try {
            csvFile = new File(filePath + File.separator + fileName);
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            csvFile.createNewFile();

            // GB2312使正确读取分隔符","
            csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);


            // 写入文件头部标题行
            csvWriter.write(String.join(",", headArr));
            csvWriter.newLine();

            // 写入文件内容
            for (String s : list) {
                csvWriter.write(s);
                csvWriter.newLine();
            }
            csvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }

    @Override
    public void outCsvStream(HttpServletResponse response, File csvFile) {
        try{
            java.io.OutputStream out = response.getOutputStream();
            byte[] b = new byte[10240];
            java.io.File fileLoad = new java.io.File(csvFile.getCanonicalPath());
            response.reset();
            response.setContentType("application/csv");
            response.setHeader("content-disposition", "attachment; filename=export.csv");
            java.io.FileInputStream in = new java.io.FileInputStream(fileLoad);
            int n;
            //为了保证excel打开csv不出现中文乱码
            out.write(new   byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
            while ((n = in.read(b)) != -1) {
                //每次写入out1024字节
                out.write(b, 0, n);
            }
            in.close();
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean deleteFile(File file) {
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

//    public static void main(String[] args) {
//        ClearBillCSVDTO clearBillCSVDTO = new ClearBillCSVDTO();
//        clearBillCSVDTO.setAccountCode("12312321");
//        clearBillCSVDTO.setContactName("name");
//        clearBillCSVDTO.setDueDate("20191227");
//        clearBillCSVDTO.setEmailAddress("email");
//        clearBillCSVDTO.setCurrency("AUD");
//        List<ClearBillCSVDTO> list = new ArrayList<>();
//        list.add(clearBillCSVDTO);
//
//        String fileName = "billTest.csv";
//        String path  = "D:/file/";
//        createClearCsvFile(fileName,path,list);
//    }

}
