package org.apache.iotdb.ui.controller;

import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.FilePreviewService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description 读取配置文件
 * @Author yuTao
 * @Date 2022/7/6 17:42
 **/
@CrossOrigin
@RestController
@Api(value = "配置日志文件预览")
@RequestMapping(value = "/api/file/preview")
public class FilePreviewController {

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private FilePreviewService filePreviewService;

	@ApiOperation(value = "/api/file/preview/getSelectNode", notes = "获取所选节点ip")
	@RequestMapping(value = "/getSelectNode", method = { RequestMethod.POST })
	public BaseVO<Object> getSelectNode(@RequestParam(value = Task.SETTING_CONNECTID) Long id) {
		Connect connect = connectDao.selectWithSetting(id);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, null);
		} else {
			return BaseVO.success(connect.getHost());
		}
	}

	@ApiOperation(value = "/api/file/preview/configFileContent", notes = "预览路径下所有配置文件")
	@RequestMapping(value = "/configFileContent", method = { RequestMethod.POST })
	public BaseVO<Object> configFileContent(@RequestParam("fileName") String fileName, @RequestParam("ip") String ip,
			@RequestParam(value = Task.SETTING_CONNECTID) Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(id);
		Connect connect = connectDao.selectOneWithSetting(c);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, null);
		} else {
			return filePreviewService.configFileContent(fileName, ip, connect);
		}
	}

	@ApiOperation(value = "/api/file/preview/logFileContent", notes = "预览路径下所有日志文件")
	@RequestMapping(value = "/logFileContent", method = { RequestMethod.POST })
	public BaseVO<Object> logFileContent(@RequestParam("fileName") String fileName,
			@RequestParam(value = "codeMirrorCount", defaultValue = "0") int codeMirrorCount,
			@RequestParam(value = "logCount", defaultValue = "0") int logCount, @RequestParam("ip") String ip,
			@RequestParam(value = "downMore", defaultValue = "true") boolean downMore,
			@RequestParam(value = Task.SETTING_CONNECTID) Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(id);
		Connect connect = connectDao.selectOneWithSetting(c);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, null);
		} else {
			return filePreviewService.logFileContent(fileName, codeMirrorCount, logCount, ip, downMore, connect);
		}
	}

	@ApiOperation(value = "/api/file/preview/getFileNames", notes = "读取路径下所有配置和日志文件列表")
	@RequestMapping(value = "/getFileNames", method = { RequestMethod.POST })
	public BaseVO<Object> getFileNames(@RequestParam("ip") String ip, @RequestParam("fileType") String fileType,
			@RequestParam(value = Task.SETTING_CONNECTID) Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(id);
		Connect connect = connectDao.selectOneWithSetting(c);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, null);
		} else {
			return filePreviewService.getFileNames(ip, fileType, connect);
		}
	}

}
