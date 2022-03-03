package com.uwallet.pay.main.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClearBillCSVDTO {

    private  String ContactName;
    private  String EmailAddress ;
    private  String POAddressLine1 ;
    private  String POAddressLine2 ;
    private  String POAddressLine3 ;
    private  String POAddressLine4 ;
    private  String POCity ;
    private  String PORegion ;
    private  String POPostalCode ;
    private  String POCountry ;
    private  String InvoiceNumber ;
    private  String InvoiceDate ;
    private  String DueDate ;
    private  String InventoryItemCode ;
    private  String Description ;
    private  String Quantity ;
    private  String UnitAmount ;
    private  String AccountCode ;
    private  String TaxType ;
    private  String TrackingName1 ;
    private  String TrackingOption1 ;
    private  String TrackingName2 ;
    private  String TrackingOption2 ;
    private  String Currency ;


    public String toRow(){
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                this.ContactName == null ? "" :this.ContactName ,
                this.EmailAddress == null ? "" :this.EmailAddress ,
                //edit by zhangzeyuan 字符串含有逗号会导致错位
                this.POAddressLine1 == null ? "" : this.POAddressLine1.replace("," , " "),
                this.POAddressLine2 == null ? "" :this.POAddressLine2.replace("," , " "),
                this.POAddressLine3 == null ? "" :this.POAddressLine2.replace("," , " "),
                this.POAddressLine4 == null ? "" :this.POAddressLine2.replace("," , " "),
                this.POCity == null ? "" :this.POCity,
                this.PORegion == null ? "" :this.PORegion,
                this.POPostalCode == null ? "" :this.POPostalCode,
                this.POCountry == null ? "" :this.POCountry,
                this.InvoiceNumber == null ? "" :this.InvoiceNumber,
                this.InvoiceDate == null ? "" :this.InvoiceDate,
                this.DueDate == null ? "" :this.DueDate,
                this.InventoryItemCode == null ? "" :this.InventoryItemCode,
                this.Description == null ? "" :this.Description,
                this.Quantity == null ? "" :this.Quantity,
                this.UnitAmount == null ? "" :this.UnitAmount,
                this.AccountCode == null ? "" :this.AccountCode,
                this.TaxType == null ? "" :this.TaxType,
                this.TrackingName1 == null ? "" :this.TrackingName1,
                this.TrackingOption1 == null ? "" :this.TrackingOption1,
                this.TrackingName2 == null ? "" :this.TrackingName2,
                this.TrackingOption2 == null ? "" :this.TrackingOption2,
                this.Currency== null ? "" :this.Currency
                );
    }

}
