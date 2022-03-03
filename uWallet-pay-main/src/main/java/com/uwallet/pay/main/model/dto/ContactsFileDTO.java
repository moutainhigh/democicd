package com.uwallet.pay.main.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactsFileDTO {

    private String ContactName;
    private String AccountNumber;
    private String EmailAddress;
    private String FirstName;
    private String LastName;
    private String POAttentionTo;
    private String POAddressLine1;
    private String POAddressLine2;
    private String POAddressLine3;
    private String POAddressLine4;
    private String POCity;
    private String PORegion;
    private String POPostalCode;
    private String POCountry;
    private String SAAttentionTo;
    private String SAAddressLine1;
    private String SAAddressLine2;
    private String SAAddressLine3;
    private String SAAddressLine4;
    private String SACity;
    private String SARegion;
    private String SAPostalCode;
    private String SACountry;
    private String PhoneNumber;
    private String FaxNumber;
    private String MobileNumber;
    private String DDINumber;
    private String SkypeName;
    private String BankAccountName;
    private String BankAccountNumber;
    private String BankAccountParticulars;
    private String TaxNumber;
    private String AccountsReceivableTaxCodeName;
    private String AccountsPayableTaxCodeName;
    private String Website;
    private String LegalName;
    private String Discount;
    private String CompanyNumber;
    private String DueDateBillDay;
    private String DueDateBillTerm;
    private String DueDateSalesDay;
    private String DueDateSalesTerm;
    private String SalesAccount;
    private String PurchasesAccount;
    private String TrackingName1;
    private String SalesTrackingOption1;
    private String PurchasesTrackingOption1;
    private String TrackingName2;
    private String SalesTrackingOption2;
    private String PurchasesTrackingOption2;
    private String BrandingTheme;
    private String DefaultTaxBills;
    private String DefaultTaxSales;
    private String Person1FirstName;
    private String Person1LastName;
    private String Person1Email;
    private String Person1IncludeInEmail;
    private String Person2FirstName;
    private String Person2LastName;
    private String Person2Email;
    private String Person2IncludeInEmail;
    private String Person3FirstName;
    private String Person3LastName;
    private String Person3Email;
    private String Person3IncludeInEmail;
    private String Person4FirstName;
    private String Person4LastName;
    private String Person4Email;
    private String Person4IncludeInEmail;
    private String Person5FirstName;
    private String Person5LastName;
    private String Person5Email;
    private String Person5IncludeInEmail;

    public String toRow(){
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                this.ContactName == null ? "" :this.ContactName ,
                this.AccountNumber == null ? "" :this.AccountNumber ,
                this.EmailAddress == null ? "" :this.EmailAddress ,
                this.FirstName == null ? "" :this.FirstName ,
                this.LastName == null ? "" :this.LastName ,
                this.POAttentionTo == null ? "" :this.POAttentionTo ,
                this.POAddressLine1 == null ? "" :this.POAddressLine1 ,
                this.POAddressLine2 == null ? "" :this.POAddressLine2 ,
                this.POAddressLine3 == null ? "" :this.POAddressLine3 ,
                this.POAddressLine4 == null ? "" :this.POAddressLine4 ,
                this.POCity == null ? "" :this.POCity ,
                this.PORegion == null ? "" :this.PORegion ,
                this.POPostalCode == null ? "" :this.POPostalCode ,
                this.POCountry == null ? "" :this.POCountry ,
                this.SAAttentionTo == null ? "" :this.SAAttentionTo ,
                this.SAAddressLine1 == null ? "" :this.SAAddressLine1 ,
                this.SAAddressLine2 == null ? "" :this.SAAddressLine2 ,
                this.SAAddressLine3 == null ? "" :this.SAAddressLine3 ,
                this.SAAddressLine4 == null ? "" :this.SAAddressLine4 ,
                this.SACity == null ? "" :this.SACity ,
                this.SARegion == null ? "" :this.SARegion ,
                this.SAPostalCode == null ? "" :this.SAPostalCode ,
                this.SACountry == null ? "" :this.SACountry ,
                this.PhoneNumber == null ? "" :this.PhoneNumber ,
                this.FaxNumber == null ? "" :this.FaxNumber ,
                this.MobileNumber == null ? "" :this.MobileNumber ,
                this.DDINumber == null ? "" :this.DDINumber ,
                this.SkypeName == null ? "" :this.SkypeName ,
                this.BankAccountName == null ? "" :this.BankAccountName ,
                this.BankAccountNumber == null ? "" :this.BankAccountNumber ,
                this.BankAccountParticulars == null ? "" :this.BankAccountParticulars ,
                this.TaxNumber == null ? "" :this.TaxNumber ,
                this.AccountsReceivableTaxCodeName == null ? "" :this.AccountsReceivableTaxCodeName ,
                this.AccountsPayableTaxCodeName == null ? "" :this.AccountsPayableTaxCodeName ,
                this.Website == null ? "" :this.Website ,
                this.LegalName == null ? "" :this.LegalName ,
                this.Discount == null ? "" :this.Discount ,
                this.CompanyNumber == null ? "" :this.CompanyNumber ,
                this.DueDateBillDay == null ? "" :this.DueDateBillDay ,
                this.DueDateBillTerm == null ? "" :this.DueDateBillTerm ,
                this.DueDateSalesDay == null ? "" :this.DueDateSalesDay ,
                this.DueDateSalesTerm == null ? "" :this.DueDateSalesTerm ,
                this.SalesAccount == null ? "" :this.SalesAccount ,
                this.PurchasesAccount == null ? "" :this.PurchasesAccount ,
                this.TrackingName1 == null ? "" :this.TrackingName1 ,
                this.SalesTrackingOption1 == null ? "" :this.SalesTrackingOption1 ,
                this.PurchasesTrackingOption1 == null ? "" :this.PurchasesTrackingOption1 ,
                this.TrackingName2 == null ? "" :this.TrackingName2 ,
                this.SalesTrackingOption2 == null ? "" :this.SalesTrackingOption2 ,
                this.PurchasesTrackingOption2 == null ? "" :this.PurchasesTrackingOption2 ,
                this.BrandingTheme == null ? "" :this.BrandingTheme ,
                this.DefaultTaxBills == null ? "" :this.DefaultTaxBills ,
                this.DefaultTaxSales == null ? "" :this.DefaultTaxSales ,
                this.Person1FirstName == null ? "" :this.Person1FirstName ,
                this.Person1LastName == null ? "" :this.Person1LastName ,
                this.Person1Email == null ? "" :this.Person1Email ,
                this.Person1IncludeInEmail == null ? "" :this.Person1IncludeInEmail ,
                this.Person2FirstName == null ? "" :this.Person2FirstName ,
                this.Person2LastName == null ? "" :this.Person2LastName ,
                this.Person2Email == null ? "" :this.Person2Email ,
                this.Person2IncludeInEmail == null ? "" :this.Person2IncludeInEmail ,
                this.Person3FirstName == null ? "" :this.Person3FirstName ,
                this.Person3LastName == null ? "" :this.Person3LastName ,
                this.Person3Email == null ? "" :this.Person3Email ,
                this.Person3IncludeInEmail == null ? "" :this.Person3IncludeInEmail ,
                this.Person4FirstName == null ? "" :this.Person4FirstName ,
                this.Person4LastName == null ? "" :this.Person4LastName ,
                this.Person4Email == null ? "" :this.Person4Email ,
                this.Person4IncludeInEmail == null ? "" :this.Person4IncludeInEmail ,
                this.Person5FirstName == null ? "" :this.Person5FirstName ,
                this.Person5LastName == null ? "" :this.Person5LastName ,
                this.Person5Email == null ? "" :this.Person5Email ,
                this.Person5IncludeInEmail == null ? "" :this.Person5IncludeInEmail

                );
    }

}
