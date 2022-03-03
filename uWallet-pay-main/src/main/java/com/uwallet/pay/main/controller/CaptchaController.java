package com.uwallet.pay.main.controller;

import cn.hutool.core.codec.Base64;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.filter.PassToken;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("captcha")
public class CaptchaController {

	@Autowired
	private RedisUtils redisUtils;

	private Random random = new Random();
	/**
	 * 随机产生的字符串
	 */
	private String randString = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
	/**
	 * 图片宽
	 */
	private int width = 80;
	/**
	 * 图片高
	 */
	private int height = 26;
	/**
	 * 干扰线数量
	 */
	private int lineSize = 40;
	/**
	 * 随机产生字符数量
	 */
	private int stringNum = 4;

	private static int captchaExpires = 3 * 60;

	@PassToken
	@ApiOperation(value = "生成验证码并保存到redis里", notes = "生成验证码并保存到redis里")
	@GetMapping(value = "getcaptcha")
	public Object getCaptcha(HttpServletResponse response) {
		// BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
		Graphics g = image.getGraphics();
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));
		g.setColor(getRandColor(110, 133));
		// 绘制干扰线
		for (int i = 0; i <= lineSize; i++) {
			drowLine(g);
		}
		// 绘制随机字符
		String randomString = "";
		for (int i = 1; i <= stringNum; i++) {
			randomString = drowString(g, randomString, i);
		}
		g.dispose();
		String captchaCodeUUID ="captchaCode-" + UUID.randomUUID().toString();
		// 将验证码以<key,value>形式缓存到redis
		redisUtils.set(captchaCodeUUID, randomString, captchaExpires);
		// 将验证码以<key,value>形式放到map中，再缓存到redis，但没法控制map里每条记录的过期时间
		Map<String, Object> captchaCodeMap = new HashMap<String, Object>();
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", bao);
			captchaCodeMap.put("uuid", captchaCodeUUID);
			captchaCodeMap.put("src", Base64.encode(bao.toByteArray()));
			return R.success(captchaCodeMap);
		} catch (IOException e) {
			return null;
		}
	}

	@PassToken
	@ApiOperation(value = "图片验证码校验", notes = "图片验证码校验")
	@GetMapping(value = "/verifyCaptcha",name = "图片验证码校验")
	public Object verifyCaptchaCode(HttpServletRequest request) {
		String uuid = request.getParameter("uuid");
		String captchaCode = request.getParameter("captchaCode");
		if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(captchaCode)) {
			return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),"Request parameter error");
		}
		if (redisUtils.hasKey(uuid)) {
			// 获取redis里的保存的验证码
			String captchaCodeInRedis = (String) redisUtils.get(uuid);
			if (captchaCodeInRedis.equalsIgnoreCase(captchaCode)) {
				return R.success();
			} else {
				return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),"Incorrect verification code");
			}
		} else {
			return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),"Verification code expired");
		}
	}

	/**
	 * 获得字体
	 * @return
	 */
	private Font getFont() {
		return new Font("Fixedsys", Font.CENTER_BASELINE, 18);
	}

	/**
	 * 获得颜色
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc - 16);
		int g = fc + random.nextInt(bc - fc - 14);
		int b = fc + random.nextInt(bc - fc - 18);
		return new Color(r, g, b);
	}

	/**
	 * 绘制字符串
	 * @param g
	 * @param randomString
	 * @param i
	 * @return
	 */
	private String drowString(Graphics g, String randomString, int i) {
		g.setFont(getFont());
		g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
		String rand = String.valueOf(getRandomString(random.nextInt(randString.length())));
		randomString += rand;
		g.translate(random.nextInt(3), random.nextInt(3));
		g.drawString(rand, 13 * i, 16);
		return randomString;
	}

	/**
	 * 绘制干扰线
	 * @param g
	 */
	private void drowLine(Graphics g) {
		int x = random.nextInt(width);
		int y = random.nextInt(height);
		int xl = random.nextInt(13);
		int yl = random.nextInt(15);
		g.drawLine(x, y, x + xl, y + yl);
	}

	/**
	 * 获取随机的字符
	 * @param num
	 * @return
	 */
	public String getRandomString(int num) {
		return String.valueOf(randString.charAt(num));
	}
}
