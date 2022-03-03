package com.uwallet.pay.main.filter;


import com.stripe.Stripe;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class WebServletRequestReplacedFilter implements Filter {


    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        // Stripe 不需要处理
        if (request instanceof HttpServletRequest){
            HttpServletRequest request1=(HttpServletRequest)request;
            String header = request1.getHeader("Stripe-Signature");
            if (StringUtils.isNotEmpty(header)){
                chain.doFilter(request, response);
                return;
            }
        }
        if (request instanceof HttpServletRequest) {
            requestWrapper = new WebHttpServletRequestWrapper((HttpServletRequest) request);
        }
        //获取请求中的流如何，将取出来的字符串，再次转换成流，然后把它放入到新request对象中。
        // 在chain.doFiler方法中传递新的request对象
        if(requestWrapper == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

}
