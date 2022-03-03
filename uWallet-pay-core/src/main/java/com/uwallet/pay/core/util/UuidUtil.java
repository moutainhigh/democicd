package com.uwallet.pay.core.util;

import java.util.UUID;

public class UuidUtil {

	/**
	 * 获取32位的UUID
	 * @return
	 */
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

}
