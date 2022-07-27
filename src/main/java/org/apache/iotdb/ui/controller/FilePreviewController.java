package org.apache.iotdb.ui.controller;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.iotdb.ui.exception.PlatFormException;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.FilePreviewService;
import org.apache.iotdb.ui.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private FilePreviewService filePreviewService;

    @ApiOperation(value = "/api/file/preview/getAllNode", notes = "获取所有节点")
    @RequestMapping(value = "/getAllNode", method = {RequestMethod.POST})
    public BaseVO<Object> getAllNode() {
        List<String> nodes = new ArrayList<>();
        nodes.add("172.20.45.128");
        nodes.add("172.20.45.127");
        return BaseVO.success(nodes);
    }

    @ApiOperation(value = "/api/file/preview/configFileContent", notes = "预览路径下所有配置文件")
    @RequestMapping(value = "/configFileContent", method = {RequestMethod.POST})
    public BaseVO<Object> fileContent(@RequestParam("fileName") String fileName,@RequestParam("ip") String ip) {
        return filePreviewService.configFileContent(fileName,ip);
    }

    @ApiOperation(value = "/api/file/preview/logFileContent", notes = "预览路径下所有日志文件")
    @RequestMapping(value = "/logFileContent", method = {RequestMethod.POST})
    public BaseVO<Object> logFileContent(@RequestParam("fileName") String fileName,@RequestParam(value = "codeMirrorCount",defaultValue = "0") int codeMirrorCount,
                                         @RequestParam(value = "logCount",defaultValue = "0") int logCount,@RequestParam("ip") String ip,
            @RequestParam(value = "downMore",defaultValue = "true") boolean downMore) {
        return filePreviewService.logFileContent(fileName,codeMirrorCount,logCount,ip,downMore);
    }

    @ApiOperation(value = "/api/file/preview/getFileNames", notes = "读取路径下所有配置和日志文件列表")
    @RequestMapping(value = "/getFileNames", method = {RequestMethod.POST})
    public BaseVO<Object> getFileNames(@RequestParam("ip") String ip) {
        return filePreviewService.getFileNames(ip);
    }

}
