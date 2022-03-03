package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author baixinyue
 * @desctiption 个体或其他类型商户请求docusign
 */
@Slf4j
@Service("soleAndOtherEntityMerchantService")
public class SoleAndOtherEntityMerchantServiceImpl extends BaseServiceImpl implements DocusignRequestService {

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private ContactPersonService contactPersonService;

    @Autowired
    private ShareholderService shareholderService;

    @Autowired
    @Lazy
    private UserService userService;

    @Value("${uWallet.docusignCallback}")
    private String docusignCallback;

    @Value("${docusignEmail}")
    private String docusignEmail;

    @Override
    public JSONObject docusignRequest(MerchantDTO merchantDTO, String docusignContractId, HttpServletRequest request) throws Exception {

        Map<String, Object> params = new HashMap<>(16);
        params.put("code", "county");
        params.put("value", merchantDTO.getCounty());
        StaticDataDTO country = staticDataService.findOneStaticData(params);

        String merchantState = "";
        if (merchantDTO.getMerchantState() != null) {
            params.clear();
            params.put("code", "merchantState");
            params.put("value", merchantDTO.getMerchantState());
            StaticDataDTO state = staticDataService.findOneStaticData(params);
            merchantState = state.getEnName();
        }

        String merchantCity = "";
        if (merchantDTO.getCity() != null) {
            params.clear();
            params.put("code", "city");
            params.put("value", merchantDTO.getCity());
            StaticDataDTO city = staticDataService.findOneStaticData(params);
            merchantCity = city.getEnName();
        }

        params.clear();
        params.put("merchantId", merchantDTO.getId());

        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);

        List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);

        JSONObject textTabs = new JSONObject();
        textTabs.put("companyInformation_businessRegisteredName", merchantDTO.getCorporateName());
        textTabs.put("companyInformation_tradingName", merchantDTO.getPracticalName());
        textTabs.put("companyInformation_streetAddress", merchantDTO.getAddress());
//        textTabs.put("companyInformation_city", merchantDTO.getCity());
        textTabs.put("companyInformation_suburb", merchantDTO.getSuburb());
        textTabs.put("companyInformation_state", merchantState);
        textTabs.put("companyInformation_postalCode", merchantDTO.getPostcode());
        textTabs.put("companyInformation_companyABNACN", merchantDTO.getAbn());
        textTabs.put("companyInformation_companyPhone", merchantDTO.getBusinessPhone());
        textTabs.put("companyInformation_companyWebsite", merchantDTO.getWebsites());

        textTabs.put("bankingInformation_accountName", merchantDTO.getAccountName());
        textTabs.put("bankingInformation_accountNumber", merchantDTO.getAccountNo());
        textTabs.put("bankingInformation_bsb", merchantDTO.getBsb());

        textTabs.put("authorisedOfficer_name", merchantDTO.getDocusignSigner());

        textTabs.put("agreementDetails_appChargeFee", merchantDTO.getAppChargeRate().multiply(new BigDecimal(100)).intValue());

        textTabs.put("companyInformation_otherText", !StringUtils.isEmpty(merchantDTO.getOtherEntity()) ? merchantDTO.getOtherEntity() : "");

        if (merchantDTO.getEstimatedAnnualSales() != null) {
            params.clear();
            params.put("code", "estimatedAnnualSales");
            params.put("value", merchantDTO.getEstimatedAnnualSales());
            StaticDataDTO estimatedAnnualSales = staticDataService.findOneStaticData(params);
            textTabs.put("latpay_approvedMonthlyVolume", estimatedAnnualSales.getEnName());
        }

        if (merchantDTO.getAvgSalesValue() != null) {
            params.clear();
            params.put("code", "avgSalesValue");
            params.put("value", merchantDTO.getAvgSalesValue());
            StaticDataDTO avgSalesValue = staticDataService.findOneStaticData(params);
            textTabs.put("latpay_averageTransactionValue", avgSalesValue.getEnName());
        }

        if (merchantDTO.getSalesValueByCard() != null) {
            params.clear();
            params.put("code", "salesValueByCard");
            params.put("value", merchantDTO.getSalesValueByCard());
            StaticDataDTO salesValueByCard = staticDataService.findOneStaticData(params);
            textTabs.put("latpay_approvedHighestTicketSize", salesValueByCard.getEnName());
        }

        JSONObject checkbox = new JSONObject();
        checkbox.put("legalStructure_" + merchantDTO.getEntityType(), true);
        checkbox.put("transactionFeePaid_" + merchantDTO.getChargeMode(), true);

        if (contactPersonDTOList != null && contactPersonDTOList.size() > 0) {
            ContactPersonDTO contactPersonDTO = contactPersonDTOList.get(0);
            textTabs.put("contactInformation_name", contactPersonDTO.getName());
            textTabs.put("contactInformation_title", contactPersonDTO.getTitle());
            textTabs.put("contactInformation_mobile", contactPersonDTO.getMobile());
            textTabs.put("contactInformation_email", contactPersonDTO.getEmail());
            textTabs.put("contactInformation_wechat", contactPersonDTO.getWechat());
        }

        boolean shareholderExist = shareholderDTOList != null && shareholderDTOList.size() > 0;

        if (shareholderExist) {

            for (int i = 0; i < shareholderDTOList.size(); i ++) {
                ShareholderDTO shareholderInfo = shareholderDTOList.get(i);

                String stateName = "";
                if (shareholderInfo.getLicenseState() != null) {
                    Map<String, Object> licenseStateParams = new HashMap<>(16);
                    licenseStateParams.put("code", "merchantState");
                    licenseStateParams.put("value", shareholderInfo.getLicenseState());
                    StaticDataDTO licenseState = staticDataService.findOneStaticData(licenseStateParams);
                    stateName = licenseState.getEnName();
                }

                switch (i) {
                    case 0:
                        textTabs.put("owners_firstName_a", shareholderInfo.getFirstName());
                        textTabs.put("owners_lastName_a", shareholderInfo.getLastName());
                        textTabs.put("owners_DOB_a", shareholderInfo.getBirth());
                        textTabs.put("owners_ownership_a", shareholderInfo.getOwnerShip());
                        if (shareholderInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_a", shareholderInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_a", shareholderInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_a", stateName);
                        }
                        break;
                    case 1:
                        textTabs.put("owners_firstName_b", shareholderInfo.getFirstName());
                        textTabs.put("owners_lastName_b", shareholderInfo.getLastName());
                        textTabs.put("owners_DOB_b", shareholderInfo.getBirth());
                        textTabs.put("owners_ownership_b", shareholderInfo.getOwnerShip());
                        if (shareholderInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_b", shareholderInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_b", shareholderInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_b", stateName);
                        }
                        break;
                    case 2:
                        textTabs.put("owners_firstName_c", shareholderInfo.getFirstName());
                        textTabs.put("owners_lastName_c", shareholderInfo.getLastName());
                        textTabs.put("owners_DOB_c", shareholderInfo.getBirth());
                        textTabs.put("owners_ownership_c", shareholderInfo.getOwnerShip());
                        if (shareholderInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_c", shareholderInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_c", shareholderInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_c", stateName);
                        }
                        break;
                    case 3:
                        textTabs.put("owners_firstName_d", shareholderInfo.getFirstName());
                        textTabs.put("owners_lastName_d", shareholderInfo.getLastName());
                        textTabs.put("owners_DOB_d", shareholderInfo.getBirth());
                        textTabs.put("owners_ownership_d", shareholderInfo.getOwnerShip());
                        if (shareholderInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_d", shareholderInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_d", shareholderInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_d", stateName);
                        }
                        break;
                    default:
                        break;
                }
                if (i == shareholderDTOList.size() - 1) {
                    break;
                }
            }

        }

        params.clear();
        params.put("merchantId", merchantDTO.getId());
        UserDTO userDTO = userService.findOneUser(params);

        JSONObject requestInfo = new JSONObject();
        requestInfo.put("signerName", merchantDTO.getDocusignSigner());
        requestInfo.put("signerEmail", docusignEmail);
        requestInfo.put("clientUserId", merchantDTO.getId());
        requestInfo.put("callBackUrl", docusignCallback);
        requestInfo.put("textTabs", textTabs);
        requestInfo.put("checkbox", checkbox);
        requestInfo.put("templateId", docusignContractId);
        requestInfo.put("authorisedTitle", merchantDTO.getAuthorisedTitle());

        return requestInfo;
    }

}
