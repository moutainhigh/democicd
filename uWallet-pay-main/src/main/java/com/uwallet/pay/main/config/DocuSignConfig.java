package com.uwallet.pay.main.config;

/**
 * DocuSign相关配置参数
 * 控制台页面,需要登录账号
 *https://admindemo.DocuSign.com/api-integrator-key?accountId=689ffe6a-7a29-4b61-8916-9d7dd85ab62c
 *
 * @author aaron.S
 * @since 2020/8/18
 */
public class DocuSignConfig {
    //模板创建账号相关参数=============================================================
    /**
     * 账号ID,登录后控制台获取
     * 1003298052
     */
    public static final String accountId_test = "11073843";
    public static final String accountId_prod = "1003298052";
    /**
     * Api account Id
     */
    public static final String apiAccountId_test = "758c32e8-93da-4ff6-b6db-7affcc7cde5a";
    public static final String apiAccountId_prod = "d778cb10-778a-4703-9b04-9827eb5f9ee7";

    //envelope相关参数==================================================================
    /**
     * #验权方式,固定写none
     */
    public static final String authenticationMethod = "none";
    /**
     * apiBasePath
     */
    public static final String basePath_test = "https://demo.docusign.net";
    public static final String basePath_prod = "https://au.docusign.net";
    //JWT(获取Token)账号相关参数=============================================================
    /**
     * #验权方式,固定写none
     */
    public static final String accountName = "U Payment Solutions Pty Ltd";
    /**
     *DS_AUTH_SERVER,生产用:account.docusign.com
     */
    public static final String DS_AUTH_SERVER_test = "account-d.docusign.com";
    public static final String DS_AUTH_SERVER_prod = "account.docusign.com";

    /**
     *	Integration Key--->DS_CLIENT_ID, GUID格式的id,控制台 admin,api and keys选项下页面获取
     */
    public static final String DS_CLIENT_ID_test = "d72dced7-8d63-45db-b699-cb55dbe6841a";
    public static final String DS_CLIENT_ID_prod = "d72dced7-8d63-45db-b699-cb55dbe6841a";
    /**
     *DS_IMPERSONATED_USER_GUID
     * 对应: API Username
     */
    public static final String DS_IMPERSONATED_USER_GUID_test = "e75f8f14-855f-4e57-96d0-e0963cb9dc3d";
    public static final String DS_IMPERSONATED_USER_GUID_prod = "50c18ad7-3e1f-434b-bc50-51d3516e4bee";

    /**
     *private Key 生成integrationKey时会获取到
     * Keypair ID: 6675a822-c8a2-4ca6-aff1-78ea0ba5c720
     * ppk: -----BEGIN PUBLIC KEY-----
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh2SHuXrns2EtqptUhDIQ
     * I9bjBkZ5G7AJ6S9/apZuxBMO+ybOLn47K1puJJmTTO0OWcPOeO69tLJwrIT4Jk3v
     * ydNGzxGQFdNsz+GuW/nMY2WiNjscEK7yMt19UG05x4wWuX/42KHuAHhcQD+qXeKp
     * umkt+mQY+zYXJv+PbGsmj7mCabOYMeVGXlH7MkJvADR2hF7RLfmLo7VxzegM9RfL
     * kL3dA3ZW56woHfd+RgxbOWmd1w9wymIuEjn/QHvxPoe73309emNiWaX4ptIYgMYp
     * Nb714FQ3xH4IgoKp7362vstqRi56gJzt3POCWxYR9YgVvEXu+VTe6EnHZu/ueSqY
     * VQIDAQAB
     * -----END PUBLIC KEY-----
     */
    /**
     * 生产用 公私钥:
     * Keypair ID: 081f7bb9-dfed-4615-a5dd-e83f8102a09d
     */
    public static final String publicKeyString_prod = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlnGUTAHrNjUn2I92yksl\n" +
            "lidFLAFiKXo+aDJdtxszWO4ruGMg24MmoiVmPFB/6DYPkD4TnkDBcsrrmTvjJZqT\n" +
            "Spu2aFZ6wbARbJDrArFrmw9ViL0161QfbneW3EG9DJB1aW/xYuUL6m9e3f6i+ZRa\n" +
            "fOX61vGSXIx1jmeK2Ruyu6l6ynML7Ya4qG2GeY89nsq0/gIQzsjvXqnylY4RLLuc\n" +
            "oLi/NgdG7NZgy0ifRlklHqQ1BNNkhZDaVJf309kNeiJ8UzLMArR3i2qIfkjdatKg\n" +
            "9OEmYLR+VTugdz8gnjNghQE70SqJ8Jmp0bWxyxoD77GOE4h9PZyGuF9O0Is47rzN\n" +
            "+QIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";
    public static final String privateKeyString_prod = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEogIBAAKCAQEAlnGUTAHrNjUn2I92yksllidFLAFiKXo+aDJdtxszWO4ruGMg\n" +
            "24MmoiVmPFB/6DYPkD4TnkDBcsrrmTvjJZqTSpu2aFZ6wbARbJDrArFrmw9ViL01\n" +
            "61QfbneW3EG9DJB1aW/xYuUL6m9e3f6i+ZRafOX61vGSXIx1jmeK2Ruyu6l6ynML\n" +
            "7Ya4qG2GeY89nsq0/gIQzsjvXqnylY4RLLucoLi/NgdG7NZgy0ifRlklHqQ1BNNk\n" +
            "hZDaVJf309kNeiJ8UzLMArR3i2qIfkjdatKg9OEmYLR+VTugdz8gnjNghQE70SqJ\n" +
            "8Jmp0bWxyxoD77GOE4h9PZyGuF9O0Is47rzN+QIDAQABAoH/CluHejQnnXPLan12\n" +
            "EDdcPxo1CPKPb4ubAFF3YkPdJSJAoFEy0eoVntQ0VbK1G6edKrEbaKnMp7qQvdwB\n" +
            "p3Uc7uYJiOVrTipxCKhxhZ3xapuDqAZez/TatSBGoqNkAJlxe/DZDZX3XAyO23nM\n" +
            "fqJYGIE848995CFHdqZqSMg/ZewJf9OMCiJWA5hCy4tkkaTdHkT6t8LJ4MRQGFT3\n" +
            "e3jjf9g/y1QpqcH2ADY+gU0RGsp5VryyhUqnThu5ya1It3KkmLkPiLFYp7yn2Hwx\n" +
            "XWZeFe5yF7fVvPc78139s/7rJyqGw0EeMORE7PQBXfRAv+aXmDvGwPBoRGFurZ/u\n" +
            "z6MBAoGBAPOnVBGobySk5FzhXevdjHou+ZKlHtg6r65EmmfTCOaoH41TP9QxRjN7\n" +
            "lOR576+lnWSU9d/d6t4vBsQWdFBCV5GFvcbpovDnvkz521l+YTKSK0KH8Ywvuouh\n" +
            "dyoHI0B1JiefyOhT+xy/rgeAn8jinDuUQM14Vf542gjEluloALEBAoGBAJ4RIe+C\n" +
            "AchUbZDeMjiBKWum+IzfXb35aeCl/3k2RdU/peywoCEmBjRzUcKKZMiLbvogi0px\n" +
            "VzMGh60UJCZBIkpieC7B3pzRq1ku3YYaTWASHOyTNJC9R2JN9GKBd6mz2HkVs0Bc\n" +
            "7wPsTalFnp3ECulCRBzkpGvVqlHm0DNorKT5AoGBAMxrbpUCA31y+kUSr4Hb9qfs\n" +
            "EWd/MUmosww6cnD5FVavZwli9wJH9OXsoRavVvC+ujZQ45hWz15phfut+xf7EQz+\n" +
            "cCIakqIbTAW4+tcyuMl86N6Av58lzBSp+xg/tMu3fbNw01ZYXy3Xg2K3/1bDCODT\n" +
            "mSrJ8xv0uLjydsqMf0gBAoGBAJvXPGQNA4DlbJjV2SHIhnPyzT7mwl+Y1gB86SIy\n" +
            "lisnc4mmolnMSXXQ9J0fITpv7lyBVZNxp41RNNc58mIc+Zfo+aDv13E5bvygrhuX\n" +
            "Nw4vDYFZqQvC8exD+1xbVQVloVnQQUiF18lAY1kuoLHfJPrRMKJj8o+2uxPAwyjM\n" +
            "XNrRAoGAOiEWm7k4KdjAsVuUSJEwFH3LqM+NdISMXH0UC/Ajwklq5QANt3ziR/aO\n" +
            "dmbg5luRySPQmZ2pqUtq0NOGOCu8zwe/kvIPimh+nXN0iOfo6ctGzmRF0n9+Y5cX\n" +
            "LFNSNK0h55fUH0HUekMVpJJjvPuc4PxLdOqn2VHMDCwMCpcc118=\n" +
            "-----END RSA PRIVATE KEY-----\n";
    public static final String privateKeyString_test = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEogIBAAKCAQEAh2SHuXrns2EtqptUhDIQI9bjBkZ5G7AJ6S9/apZuxBMO+ybO\n" +
            "Ln47K1puJJmTTO0OWcPOeO69tLJwrIT4Jk3vydNGzxGQFdNsz+GuW/nMY2WiNjsc\n" +
            "EK7yMt19UG05x4wWuX/42KHuAHhcQD+qXeKpumkt+mQY+zYXJv+PbGsmj7mCabOY\n" +
            "MeVGXlH7MkJvADR2hF7RLfmLo7VxzegM9RfLkL3dA3ZW56woHfd+RgxbOWmd1w9w\n" +
            "ymIuEjn/QHvxPoe73309emNiWaX4ptIYgMYpNb714FQ3xH4IgoKp7362vstqRi56\n" +
            "gJzt3POCWxYR9YgVvEXu+VTe6EnHZu/ueSqYVQIDAQABAoIBAAwyckezWsw+JO8J\n" +
            "r0N5fxq7cRhPoPltknb/YGspwjDHxsG5bTmjBZGSQ/noG+0CLhjIGwGqAE0XBi+E\n" +
            "ixZoTwHPlsFww5ozwcm4ewyuJEuQDFz2VkDqgcczTObB8EKZzDJJ5REfBWHldeoT\n" +
            "0jVDBidU8l9sRRPce5l3zGPoA0FPQ3NT2d2Wf5chhIpgbaF9uBLlukMLPJ4GrsAE\n" +
            "yPGZwFukJ5ipkWeaM79rvcwjBR8vGcIe93aFteEmKpcmdUJesegd0FCHQ19eCXZD\n" +
            "lXnpT7j+P4Nmj+mjifAtvtHIgOdkpNTvdKkpmuimB9hXlafZ7/3tHDGX4W6VuVde\n" +
            "1bf67MECgYEAvgi9JXuH71VKt19OPSpNxt4KABzv6DfpA+kUs2bJqMaf4reVx0aA\n" +
            "ePkibz26VLvWuxh7+SdwwwiqHs6zAN2+VLtqLr6f+141W7ULtGD1fAgG3z9Xxpza\n" +
            "OeNKVzT47rOA2ivV5zflawR8NLFijfZS+EU1Jr6rHLaKVN0GF9ldCAMCgYEAtmQd\n" +
            "a7ZZTLtHR3iMeedyaY1zdzC2Zqlmldd9oLyrtaJgcF/yKZhXl9USOxeMP0iqFge8\n" +
            "rYMm/oMkhxCcsY5nZXVB3j3c5wqkQ0MS4+LQ89fHrRrTXbh8zRK+9Rcreacl9Po/\n" +
            "LwIHHibSKlnxWQIADHlFw23TQN+Ss+D5OjstyscCgYA4XcE0z/pXDBZ4Y6WCUPz2\n" +
            "4r9Wnz6tw3+zMDx6ph9kl12vUsLzc78BouwK0OVm4rxjjHShR4iT4QsMzkScGimt\n" +
            "WlW5fcNYPO5YwE+zdhr6aisXpuMzPy0fhIJ1cXnw2395twzgY10CLDdnLcJM12Zu\n" +
            "y6BjWKTKhH5tg42eH1QP3wKBgD2/OQKdMjA6+s+kbRJcG6zGWX/Rev7YfMVftcUz\n" +
            "+oG8Xh/NK6OPUqAxX0GoIC/QftyEdAjd7NafOT+MjS/DQRflEY6WrPF+9ctbqsqB\n" +
            "oDHdLFlbSpFhAsc+gG1jEbHWt4/vJHLaoeRASkhSONXkSnXunIKj7K/6wKSrFlTz\n" +
            "cfZDAoGACrnrCSfmH5J/3ImrwkJB15du0JsmDHDd+XMUy2zRAWb8+Aynw4zrsvf6\n" +
            "xfs4TzNCgGu15h4lia9vulND8Aq52YGmDJwlqep+nJHaU+wxseeOCCV+O2dxXiW8\n" +
            "//5Bidw5zDlR1zdMdrKSYUqrF0zwbgP981Bw6hIIk6ySBSZvudA=\n" +
            "-----END RSA PRIVATE KEY-----\n";

    /*
    每个信封都需要一个role_name, 统一用该role name
     */
    public static final String DEFAULT_ROLE_NAME = "needToSign";
    public static final String DEFAULT_ROLE_CC_NAME = "needToCC";
    public static final String API_PATH = "/restapi";
    /**
     * 销售 邮箱
     */
    public static final String SALES_EMAIL = "sales@uwallet.net.au";
    public static final String SALES_NAME = "UWallet-Sales";

    /**
     * 生产 consent的返回code:
     *
     https://www.baidu.com/?code=eyJ0eXAiOiJNVCIsImFsZyI6IlJTMjU2Iiwia2lkIjoiOGFlYzFjZjQtYmE4NS00MDM5LWE1MmItYzVhODAxMjA3N2EyIn0.AQsAAAABAAYABwAAjgpT6YXYSAgAABqRmumF2EgCANeKwVAfPktDvFBR01FuS-4VAAUAAAAYAAIAAAAFAAAAHQAAAA0AJAAAAGQ3MmRjZWQ3LThkNjMtNDVkYi1iNjk5LWNiNTVkYmU2ODQxYSIAJAAAAGQ3MmRjZWQ3LThkNjMtNDVkYi1iNjk5LWNiNTVkYmU2ODQxYTAAAI4KU-mF2EgSAAEAAAALAAAAaW50ZXJhY3RpdmU3AGmvRklm7IdBkicNWUpQIAk.iaf6jqHLC-Mqx5FzFXN1gvb-TiU05_ssE7HUH0A25Y6yMTwl4-sBcpbe2zDzjycTHCyvNViKJUy-pnGviMrHN4GfX3xbJCcxuuA9CSYhCM0by_QDUQ7KU4h0uA11KzhA1NKhCvnmdBiXWIfcN7fJZ-U5-SAHuyE8rtSW4NHtQefd50-D8D62Qy0Tq8o4sQNe6MSL1ihy2-T6wTkK2_hAwVPQ7f-Nnn-E9aGLpLh6RATh7qLRHYsc-qUoszgoGveFKaUQKZn-Br1N--_gxY7WnV71T842-Bf2yQ0CP2W_xRGnRJCtfq1bGsJMRs83I8QixlApgFwsmuEggOtE5H2cCQ
     */
}
