package com.uwallet.pay.main.controller;

import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.service.MerchantService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/docusignCallBack")
@Api("docusign 回调")
public class DocusignCallBackController {

    @Autowired
    private MerchantService merchantService;

    @PassToken
    @GetMapping("/callBack/{docusignEnvelopeid}")
    public ModelAndView callBack(@PathVariable("docusignEnvelopeid") String docusignEnvelopeid,  HttpServletRequest request) throws Exception {
        return merchantService.docusignCallBack(docusignEnvelopeid, request);
    }

}
