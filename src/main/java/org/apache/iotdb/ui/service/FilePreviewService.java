package org.apache.iotdb.ui.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.iotdb.ui.config.MonitorServerConfig;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.exception.PlatFormException;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

@Service
public class FilePreviewService {

	@Autowired
	private MonitorServerConfig monitorServerConfig;

	protected static final Logger logger = LoggerFactory.getLogger(FilePreviewService.class);

	// 文件后缀
	private static final String YML = ".yml";
	private static final String PROPERTIES = ".properties";
	private static final String LOG = ".log";

	// 标签页类型
	private static final String CONFIG_FILE_TAB = "configTab";
	private static final String LOG_FILE_TAB = "logTab";

	// 向上加载行数
	private static final int DEFAULT = 500;
	// 最大限制行数
	private static final int MAX_COUNT = 10000;

	// SSH对主机的private_key的检查等级
	private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
	private static final String NO = "no";
	// Channel类型
	private static final String SFTP = "sftp";
	private static final String EXEC = "exec";
	// 编辑器内容
	private static final String CODEMIRROR = "codeMirror";
	// 编辑器行数
	private static final String CODEMIRROR_COUNT = "codeMirrorCount";
	// 文件总行数
	private static final String LOG_COUNT = "logCount";

	public BaseVO<Object> configFileContent(String fileName, String ip, Connect connect) {
		InputStream inputStream = null;
		ChannelSftp sftp = null;
		Session session = null;
		StringBuilder builder = new StringBuilder();
		try {

			JSch jsch = new JSch();
			session = jsch.getSession(connect.getSetting().getString("serverUsername"), ip, 22);
			session.setPassword(connect.getSetting().getString("serverPassword"));
			Properties config = new Properties();
			// SSH对主机的public_key的检查等级
			config.put(STRICT_HOST_KEY_CHECKING, NO);
			session.setConfig(config);
			session.connect();
			// 读取配置文件
			Channel channel = session.openChannel(SFTP);
			channel.connect();
			sftp = (ChannelSftp) channel;
			inputStream = sftp.get(new StringBuilder(connect.getSetting().getString("serverConfigFilePath"))
					.append(fileName).toString());
			byte[] data = new byte[1024];
			int i = 0;
			while ((i = inputStream.read(data)) != -1) {
				builder.append(new String(data, 0, i));
			}
		} catch (Exception e) {
			logger.error("获取配置文件失败！", e);
			return new BaseVO<>(PlatFormException.FILE_CONTENT_FAIL,
					new StringBuilder(MessageUtil.get(PlatFormException.FILE_CONTENT_FAIL)).append(":")
							.append(e.getMessage()).toString(),
					null);
		} finally {
			if (sftp != null) {
				sftp.exit();
			}
			if (session != null) {
				session.disconnect();
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("inputStream流关闭失败", e);
				}
			}
		}
		return BaseVO.success(builder);
	}

	public BaseVO<Object> logFileContent(String fileName, int codeMirrorCount, int logCount, String ip,
			boolean downMore, Connect connect) {
		InputStream inputStream = null;
		Session session = null;
		try {

			JSch jsch = new JSch();
			session = jsch.getSession(connect.getSetting().getString("serverUsername"), ip, 22);
			session.setPassword(connect.getSetting().getString("serverPassword"));
			Properties config = new Properties();
			// SSH对主机的public_key的检查等级
			config.put(STRICT_HOST_KEY_CHECKING, NO);
			session.setConfig(config);
			session.connect();
			// 读取日志文件
			return shellReadLogFile(fileName, session, codeMirrorCount, logCount, downMore,
					connect.getSetting().getString("serverLogFilePath"));
		} catch (Exception e) {
			logger.error("获取日志文件失败！", e);
			return new BaseVO<>(PlatFormException.FILE_CONTENT_FAIL,
					new StringBuilder(MessageUtil.get(PlatFormException.FILE_CONTENT_FAIL)).append(":")
							.append(e.getMessage()).toString(),
					null);
		} finally {
			if (session != null) {
				session.disconnect();
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("inputStream流关闭失败", e);
				}
			}
		}
	}

	/**
	 * @Description 通过shell从后向前读取指定行数日志
	 * @date 2022/7/15 13:38
	 * @param fileName
	 * @param session
	 * @param codeMirrorCount
	 * @param logCount
	 * @param downMore
	 * @return BaseVO<Object>
	 */
	private BaseVO<Object> shellReadLogFile(String fileName, Session session, int codeMirrorCount, int logCount,
			boolean downMore, String serverLogFilePath) throws Exception {
		BufferedReader inputContentStreamReader = null;
//        BufferedReader errInputStreamReader = null;
		StringBuilder runLog = new StringBuilder();
		Map<String, Object> resultMap = new HashMap<>(5);
		ChannelExec channelExec = (ChannelExec) session.openChannel(EXEC);
		channelExec.setPty(false);
		try {
			// 查询指定行数之间的日志
			StringBuilder contentCmd = shellCode(logCount, codeMirrorCount, fileName, downMore, serverLogFilePath);
			channelExec.setCommand(contentCmd.toString());
			// 获取执行脚本可能出现的错误日志
			channelExec.setErrStream(System.err);
			channelExec.connect(); // 执行命令
			// 3. 获取标准输入流
			inputContentStreamReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
			// 5. 记录命令执行 log
			String line = null;
			boolean firstLineFlag = true;
			int newLogCount = 0;
			while ((line = inputContentStreamReader.readLine()) != null) {
				if (firstLineFlag) {
					newLogCount = Integer.parseInt(line);
					firstLineFlag = false;
				} else {
					runLog.append(line).append("\n");
				}
			}
			// 计算编辑器行数
			if (downMore) {
				codeMirrorCount = newLogCount - (logCount - codeMirrorCount);
			} else {
				codeMirrorCount += DEFAULT;
			}
			if (codeMirrorCount > newLogCount) {
				codeMirrorCount = newLogCount;
			}
			// 编辑器行数不能超过最大限制
			if (codeMirrorCount > MAX_COUNT) {
				codeMirrorCount = MAX_COUNT;
			}
			resultMap.put(CODEMIRROR, runLog);
			resultMap.put(CODEMIRROR_COUNT, codeMirrorCount);
			resultMap.put(LOG_COUNT, newLogCount);
		} catch (Exception e) {
			logger.error("执行shell命令查询日志失败！", e);
			return new BaseVO<>(PlatFormException.FILE_CONTENT_FAIL,
					new StringBuilder(MessageUtil.get(PlatFormException.FILE_CONTENT_FAIL)).append(":")
							.append(e.getMessage()).toString(),
					null);
		} finally {
			if (inputContentStreamReader != null) {
				inputContentStreamReader.close();
			}
			channelExec.disconnect();
		}
		return BaseVO.success(resultMap);
	}

	/**
	 * @Description shell脚本代码
	 * @date 2022/7/20 17:41
	 * @param logCount
	 * @param codeMirrorCount
	 * @param fileName
	 * @param downMore
	 * @return StringBuilder
	 */
	private StringBuilder shellCode(int logCount, int codeMirrorCount, String fileName, boolean downMore,
			String serverLogFilePath) {
		StringBuilder contentCmd = new StringBuilder();
		contentCmd.append("cd ").append(serverLogFilePath).append("\n");
		contentCmd.append("codeMirrorCount=");
		contentCmd.append(codeMirrorCount);
		contentCmd.append("\n");
		if (logCount == 0 || downMore) {
			// 查询文件总行数
			contentCmd.append("logCount=`cat ");
			contentCmd.append(fileName);
			contentCmd.append("|wc -l`\n");
			if (codeMirrorCount != 0 && !downMore) {
				contentCmd.append("codeMirrorCount=0\n");
			}
		} else {
			contentCmd.append("logCount=");
			contentCmd.append(logCount);
			contentCmd.append("\n");
		}
		contentCmd.append("echo ${logCount}\n");
		contentCmd.append("if [ $codeMirrorCount -gt $logCount ]\n");
		contentCmd.append("then codeMirrorCount=${logCount}\n");
		contentCmd.append("fi\n");
		if (!downMore) {
			contentCmd.append("defaultNum=");
			contentCmd.append(DEFAULT);
			contentCmd.append("\nlet defAndMirrorCount=${defaultNum}+${codeMirrorCount}\n");
			contentCmd.append("let firstCount=${logCount}-${defAndMirrorCount}+1\n");
			contentCmd.append("let maxFirstCount=${firstCount}+");
			contentCmd.append(MAX_COUNT - 1);
			contentCmd.append("\nif [ $logCount -gt $defAndMirrorCount ]\n");
			// 如果编辑器行数达到限制数 则后置数量相应减少
			contentCmd.append("then\n");
			contentCmd.append("if [ $defAndMirrorCount -gt ");
			contentCmd.append(MAX_COUNT);
			contentCmd.append(" ]\n");
			contentCmd.append("then sed -n ${firstCount},${maxFirstCount}p ");
			contentCmd.append(fileName);
			contentCmd.append("\nelse sed -n ${firstCount},${logCount}p ");
			contentCmd.append(fileName);
			contentCmd.append("\nfi");
			contentCmd.append("\nelif [ $logCount -gt ");
			contentCmd.append(MAX_COUNT);
			contentCmd.append(" ]\n");
			contentCmd.append("then sed -n 1,");
			contentCmd.append(MAX_COUNT);
			contentCmd.append("p ");
			contentCmd.append(fileName);
			contentCmd.append("\nelse sed -n 1,${logCount}p ");
			contentCmd.append(fileName);
			contentCmd.append("\nfi");
		} else {
			contentCmd.append("firstCount=");
			contentCmd.append(logCount > codeMirrorCount ? logCount - codeMirrorCount + 1 : 1);
			contentCmd.append("\nlet codeMirrorCount=${logCount}-${firstCount}");
			// 判断如果打印的日志行数超过最大限制，则前置数量相应减少
			contentCmd.append("\nif [ $codeMirrorCount -gt ");
			contentCmd.append(MAX_COUNT);
			contentCmd.append(" ]\n");
			contentCmd.append(" then tail -n ");
			contentCmd.append(MAX_COUNT);
			contentCmd.append(" ");
			contentCmd.append(fileName);
			contentCmd.append("\nelse sed -n ${firstCount},${logCount}p ");
			contentCmd.append(fileName);
			contentCmd.append("\nfi");
		}
		return contentCmd;
	}

	public BaseVO<Object> getFileNames(String ip, String fileType, Connect connect) {
		ChannelSftp sftp = null;
		Session session = null;
		List<String> fileNames = new ArrayList<>();
		try {

			JSch jsch = new JSch();
			session = jsch.getSession(connect.getSetting().getString("serverUsername"), ip, 22);
			session.setPassword(connect.getSetting().getString("serverPassword"));
			Properties config = new Properties();
			// SSH对主机的public_key的检查等级
			config.put(STRICT_HOST_KEY_CHECKING, NO);
			session.setConfig(config);
			session.connect();
			Channel channel = session.openChannel(SFTP);
			channel.connect();
			sftp = (ChannelSftp) channel;
			Vector vector = null;
			Iterator iterator = null;
			if (CONFIG_FILE_TAB.equals(fileType)) {
				// ls命令获取配置文件名列表
				vector = sftp.ls(connect.getSetting().getString("serverConfigFilePath"));
				iterator = vector.iterator();
				while (iterator.hasNext()) {
					ChannelSftp.LsEntry configFile = (ChannelSftp.LsEntry) iterator.next();
					// 文件名称
					String configFileName = configFile.getFilename();
					// 判断文件是否为空
					SftpATTRS sftpATTRS = sftp
							.lstat(new StringBuilder(connect.getSetting().getString("serverConfigFilePath"))
									.append(configFileName).toString());
					long size = sftpATTRS.getSize();
					if (configFileName != null && size > 0
							&& (configFileName.endsWith(YML) || configFileName.endsWith(PROPERTIES))) {
						fileNames.add(configFileName);
					}
				}
			} else if (LOG_FILE_TAB.equals(fileType)) {
				// ls命令获取日志文件名列表
				vector = sftp.ls(connect.getSetting().getString("serverLogFilePath"));
				iterator = vector.iterator();
				while (iterator.hasNext()) {
					ChannelSftp.LsEntry logFile = (ChannelSftp.LsEntry) iterator.next();
					// 文件名称
					String logFileName = logFile.getFilename();
					if (logFileName != null && logFileName.endsWith(LOG)) {
						// 判断文件是否为空
						SftpATTRS sftpATTRS = sftp
								.lstat(new StringBuilder(connect.getSetting().getString("serverLogFilePath"))
										.append(logFileName).toString());
						long size = sftpATTRS.getSize();
						if (size > 0) {
							fileNames.add(logFileName);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("获取服务器文件列表失败！", e);
			return new BaseVO<>(PlatFormException.FILE_PREVIEW_FAIL,
					new StringBuilder(MessageUtil.get(PlatFormException.FILE_PREVIEW_FAIL)).append(":")
							.append(e.getMessage()).toString(),
					null);
		} finally {
			if (sftp != null && sftp.isConnected()) {
				sftp.exit();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return BaseVO.success(fileNames);
	}

}
