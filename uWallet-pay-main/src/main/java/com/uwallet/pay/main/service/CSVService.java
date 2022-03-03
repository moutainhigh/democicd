package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public interface CSVService {

    /**
     * 生成清算CSV文件
     * @param fileName
     * @param path
     * @param clearList
     */
    void createClearCsvFile(String fileName, String path, List<ClearBillCSVDTO> clearList);

    /**
     *生成CSV文件
     * @param fileName
     * @param path
     * @param headArr
     * @param merchantStringList
     * @return
     */
    File createCsvFile(String fileName, String path, String[] headArr, List<String> merchantStringList);

    /**
     * 返回文件流
     * @param response
     * @param csvFile
     */
    void outCsvStream(HttpServletResponse response, File csvFile);

    /**
     * 删除文件
     * @param csvFile
     * @return
     */
    boolean deleteFile(File csvFile);
}
