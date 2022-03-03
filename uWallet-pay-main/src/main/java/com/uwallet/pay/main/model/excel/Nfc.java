package com.uwallet.pay.main.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * @author baixinyue
 * @description nfc导入类
 * @createDate 2020/02/20
 */
@Data
@ExcelTarget("Nfc")
public class Nfc implements Serializable {

    /**
     * nfc code
     */
    @Excel(name = "nfcNo      (*The .no cannot be duplicated)")
    private String code;

    @Excel(name = "QR CODE")
    private String qrCode;

}
