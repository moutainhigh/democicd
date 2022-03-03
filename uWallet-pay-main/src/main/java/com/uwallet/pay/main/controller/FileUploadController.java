package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.util.AmazonAwsUploadUtil;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.UploadFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @description: 文件上传
 * @author: Rainc
 * @date: Created in 2020-01-13 16:22:53
 * @version: V1.0
 * @modified: Rainc
 *
 */
@RestController
@RequestMapping("/upload")
@Slf4j
@Api("文件上传")
public class FileUploadController extends BaseController {

	@Value("${spring.IntroductionPath}")
	private String IntroductionPath;

	@Value("${spring.appLogoPath}")
	private String appLogoPath;

	@Value("${spring.appBannerPath}")
	private String appBannerPath;

	@Value("${spring.topDealsPath}")
	private String topDealsPath;

	@Value("${spring.adsPath}")
	private String adsPath;

	@Value("${spring.paperPath}")
	private String paperPath;

	@Value("${spring.bankLogo}")
	private String bankLogo;

	@Autowired
	private RedisUtils redisUtils;

	@PassToken
	@PostMapping(name = "上传图片", value = "/merchantIntroduction")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object merchantIntroduction(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		if (flag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.IntroductionPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload merchantIntroduction failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
		}
	}

	@PassToken
	@PostMapping(name = "上传图片", value = "/appLogo")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object appLogo(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		if (flag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.appLogoPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload appLogo failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
		}
	}

	@PassToken
	@PostMapping(name = "上传图片", value = "/appBanner")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object appBanner(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		if (flag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.appBannerPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload appBanner failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
		}
	}


    @PassToken
    @PostMapping(name = "上传图片", value = "/appBannerNew")
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
    public Object appBannerNew(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String originalFilename = file.getOriginalFilename();
        boolean flag = UploadFileUtil.checkBannerImgFileTypeNew(originalFilename);
        if (flag) {
            String fileName;
            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
            String key = this.appBannerPath + "/" + SnowflakeUtil.generateId() + sfx;
            try {
                fileName = AmazonAwsUploadUtil.upload(file, key);
            } catch (Exception e) {
                log.error("upload appBanner failed, error message:{}", e.getMessage());
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }
            return R.success(fileName);
        } else {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
        }
    }

	@PassToken
	@PostMapping(name = "上传图片", value = "/topDeal")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object topDeal(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		if (flag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.topDealsPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload appBanner failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
		}
	}

	@PassToken
	@PostMapping(name = "上传图片", value = "/ads")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object ads(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		if (flag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.adsPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload ads failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
		}
	}

	@PassToken
	@PostMapping(name = "上传合约", value = "/paper")
	@ApiOperation(value = "上传合约", notes = "上传合约")
	@ApiImplicitParam(name = "file", value = "合约", dataType = "MultipartFile", required = true)
	public Object paper(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean fileFlag = UploadFileUtil.checkFile(originalFilename);
		boolean imgFlag = UploadFileUtil.checkImg(originalFilename);
		if (fileFlag || imgFlag) {
			String fileName;
			String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.paperPath + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
			} catch (Exception e) {
				log.error("upload paper failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.file", getLang(request)));
		}
	}

	@PassToken
	@PostMapping(name = "上传图片", value = "/bankLogo")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	@ApiImplicitParam(name = "file", value = "图片", dataType = "MultipartFile", required = true)
	public Object bankLogo(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String originalFilename = file.getOriginalFilename();
		boolean flag = UploadFileUtil.checkImg(originalFilename);
		String bankName = request.getHeader("bankName");
		String type = request.getHeader("type");
		if (StringUtils.isEmpty(bankName)) {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("bank.name.error", getLang(request)));
		}
		String sfx = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		if ("png".equals(sfx)) {
			flag = true;
		}
		if (flag) {
			String fileName;
			sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
			String key = this.bankLogo + "/" + SnowflakeUtil.generateId() + sfx;
			try {
				fileName = AmazonAwsUploadUtil.upload(file, key);
				//将图片转为二进制，将其暂时存入redis
				byte[] bytes = file.getBytes();
				redisUtils.set(bankName + "_" + type, new BASE64Encoder().encode(bytes));
			} catch (Exception e) {
				log.error("upload appBanner failed, error message:{}", e.getMessage());
				return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
			}
			return R.success(fileName);
		} else {
			return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("bank.logo.error", getLang(request)));
		}
	}

}
