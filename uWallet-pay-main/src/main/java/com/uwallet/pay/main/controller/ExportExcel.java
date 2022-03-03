package com.uwallet.pay.main.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.uwallet.pay.main.model.dto.ExportDTO;
import com.uwallet.pay.main.model.dto.UserExcelDTO;
import com.uwallet.pay.main.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Strong
 * @Date 2019/12/12
 */
@RestController
@RequestMapping("/exportExcel")
@Slf4j
public class ExportExcel {

    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/exportExcel", name="营销活动导出用户excel")
    @ApiOperation(value = "营销活动导出用户excel", notes = "营销活动导出用户excel")
    public void exportExcelTest(@RequestBody UserExcelDTO userExcelDTO, HttpServletResponse response){
        Map<String, Object> params = new HashMap<>(2);
        List<UserExcelDTO> list;
        params.put("userType","10");
        if (StringUtils.isNotBlank(userExcelDTO.getStart()) && StringUtils.isNotBlank(userExcelDTO.getEnd())) {
            params.put("start",userExcelDTO.getStart());
            params.put("end",userExcelDTO.getEnd());
            list = userService.findList(params, null, null);
        } else {
            //如果时间为空,则查询导出所有user数据
            list = userService.findList(params, null, null);
        }
        // 获取workbook对象
        if (list == null || list.size() ==0 ) {
            return;
        }
        String format;
        for (UserExcelDTO userExcel : list) {
            format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(userExcel.getCreatedDate()));
            userExcel.setDate(format);
        }
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), UserExcelDTO.class, list);
        // 判断数据
        if(workbook == null) {
            return;
        }
        // 设置excel的文件名称 不能有中文
        // 重置响应对象
        response.reset();
        String dateStr =null;
        try {
             dateStr = URLEncoder.encode(marketingActivities, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 指定下载的文件名--设置响应头
        response.setHeader("Content_Disposition", "attachment;filename=" +dateStr+".xls");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", dateStr);
        // 写出数据输出流到页面
        try {
            OutputStream output = response.getOutputStream();
            BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
            workbook.write(bufferedOutPut);
            bufferedOutPut.flush();
            bufferedOutPut.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 文件下载
     * @param response
     * @param exportDTO 文件的路径
     */
    @PostMapping(value = "/download", name="文件下载")
    @ApiOperation(value = "文件下载", notes = "文件下载")
    public void downloadFile(@RequestBody ExportDTO exportDTO, HttpServletResponse response) {
        log.info("download data:{}",exportDTO);
        try {
            // path是指欲下载的文件的路径。
            File file = new File(exportDTO.getPath());
            // 加密传输,否则文件名会乱码或者出现下划线取得文件名。
            String filename = URLEncoder.encode(file.getName(), "utf-8");
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(exportDTO.getPath()));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("FileName", filename);
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
