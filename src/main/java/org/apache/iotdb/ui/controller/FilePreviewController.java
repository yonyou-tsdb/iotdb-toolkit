package org.apache.iotdb.ui.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.FilePreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "/api/file/preview/getSelectNode", notes = "获取所选节点ip")
    @RequestMapping(value = "/getSelectNode", method = {RequestMethod.POST})
    public BaseVO<Object> getSelectNode() {
        return BaseVO.success("172.20.45.128");
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
    public BaseVO<Object> getFileNames(@RequestParam("ip") String ip,@RequestParam("fileType") String fileType) {
        return filePreviewService.getFileNames(ip,fileType);
    }

}
