package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Partner;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
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
 * @desctiption 合伙人类型商户请求docusign
 */
@Slf4j
@Service("partnerEntityMerchantService")
public class PartnerEntityMerchantServiceImpl extends BaseServiceImpl implements DocusignRequestService {

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private ContactPersonService contactPersonService;

    @Autowired
    private PartnerService partnerService;

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

        List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);

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

        boolean partnerExist = partnerDTOList != null && partnerDTOList.size() > 0;

        if (partnerExist) {

            for (int i = 0; i < partnerDTOList.size(); i++) {
                PartnerDTO partnerInfo = partnerDTOList.get(i);

                String stateName = "";
                if (partnerInfo.getLicenseState() != null) {
                    Map<String, Object> licenseStateParams = new HashMap<>(16);
                    licenseStateParams.put("code", "merchantState");
                    licenseStateParams.put("value", partnerInfo.getLicenseState());
                    StaticDataDTO licenseState = staticDataService.findOneStaticData(licenseStateParams);
                    stateName = licenseState.getEnName();
                }

                switch (i) {
                    case 0:
                        textTabs.put("owners_firstName_a", partnerInfo.getFirstName());
                        textTabs.put("owners_lastName_a", partnerInfo.getLastName());
                        textTabs.put("owners_DOB_a", partnerInfo.getBirth());
                        textTabs.put("owners_ownership_a", partnerInfo.getOwnerShip());
                        if (partnerInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_a", partnerInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_a", partnerInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_a", stateName);
                        }
                        break;
                    case 1:
                        textTabs.put("owners_firstName_b", partnerInfo.getFirstName());
                        textTabs.put("owners_lastName_b", partnerInfo.getLastName());
                        textTabs.put("owners_DOB_b", partnerInfo.getBirth());
                        textTabs.put("owners_ownership_b", partnerInfo.getOwnerShip());
                        if (partnerInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_b", partnerInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_b", partnerInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_b", stateName);
                        }
                        break;
                    case 2:
                        textTabs.put("owners_firstName_c", partnerInfo.getFirstName());
                        textTabs.put("owners_lastName_c", partnerInfo.getLastName());
                        textTabs.put("owners_DOB_c", partnerInfo.getBirth());
                        textTabs.put("owners_ownership_c", partnerInfo.getOwnerShip());
                        if (partnerInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_c", partnerInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_c", partnerInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_c", stateName);
                        }
                        break;
                    case 3:
                        textTabs.put("owners_firstName_d", partnerInfo.getFirstName());
                        textTabs.put("owners_lastName_d", partnerInfo.getLastName());
                        textTabs.put("owners_DOB_d", partnerInfo.getBirth());
                        textTabs.put("owners_ownership_d", partnerInfo.getOwnerShip());
                        if (partnerInfo.getIdType().intValue() == StaticDataEnum.ID_TYPE_0.getCode()) {
                            textTabs.put("owners_passportNumber_d", partnerInfo.getPassport());
                        } else {
                            textTabs.put("owners_driverLicenceNumber_d", partnerInfo.getIdNo());
                            textTabs.put("owners_driverLicenseState_d", stateName);
                        }
                        break;
                    default:
                        break;
                }
                if (i == partnerDTOList.size() - 1) {
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
