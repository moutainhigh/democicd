package com.uwallet.pay.main.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 根据postCode获取用所在州
 * @author xuchenglong
 * @date 2021/5/15
 */
public class FindStateByPostCodeUtils {
    public static JSONObject processStateInfo(JSONObject info) {
        String state = "unknown";
        String postcode = info.getString("postcode");
        if (StringUtils.isNotBlank(postcode) && StringUtils.isNumeric(postcode)){
            int code = Integer.parseInt(postcode);
            /*
            NSW    1000—1999 (LVRs and PO Boxes only)
                   2000—2599
                   2619—2899
                   2921—2999
             */
            if ( compare(code,1000,1999)
                    || compare(code,2000,2599)
                    || compare(code,2619,2899)
                    || compare(code,2921,2999)){
                state = "NSW";
            }
            /*
            ACT    0200—0299 (LVRs and PO Boxes only)
                   2600—2618
                   2900—2920

             */
            if ( compare(code,200,299)
                    || compare(code,2600,2618)
                    || compare(code,2900,2920)){
                state = "ACT";
            }
            /*
            VIC    3000—3999
                   8000—8999 (LVRs and PO Boxes only)

             */
            if ( compare(code,3000,3999)
                    || compare(code,8000,8999)){
                state = "VIC";
            }
            /*
            QLD    4000—4999
                   9000—9999 (LVRs and PO Boxes only)

             */
            if (compare(code,4000,4999)
                    || compare(code,9000,9999)){
                state = "QLD";
            }
            /*
            SA     5000—5799
                   5800—5999 (LVRs and PO Boxes only)

             */
            if ( compare(code,5000,5799)
                    || compare(code,5800,5999)){
                state = "SA";
            }
            /*
            WA     6000—6797
                   6800—6999 (LVRs and PO Boxes only)

             */
            if ( compare(code,6000,6797)
                    || compare(code,6800,6999)){
                state = "WA";
            }
            /*
            TAS     7000—7799
                    7800—7999 (LVRs and PO Boxes only)

             */
            if ( compare(code,7000,7799)
                    || compare(code,7800,7999)){
                state = "TAS";
            }
            /*
            NT      800—899
                    900—999 (LVRs and PO Boxes only)

             */
            if ( compare(code,800,899)
                    || compare(code,900,999)){
                state = "NT";
            }
        }
        info.put("state",state);
        info.remove("postcode");
        return info;
    }
    public static Boolean compare(int target, int biggerThan, int smallThan) {
        return target > biggerThan -1 && target < smallThan +1 ;
    }
}
