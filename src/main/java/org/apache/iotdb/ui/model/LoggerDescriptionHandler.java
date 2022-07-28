package org.apache.iotdb.ui.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import indi.mybatis.flying.models.LoggerDescriptionable;
import indi.mybatis.flying.utils.LogLevel;

public class LoggerDescriptionHandler implements LoggerDescriptionable {

	private String[] none;

	private String[] fatal;

	private String[] error;

	private String[] warn;

	private String[] info;

	private String[] debug;

	private String[] trace;

	public String[] getNone() {
		return none;
	}

	public void setNone(String[] none) {
		this.none = none;
	}

	public String[] getFatal() {
		return fatal;
	}

	public void setFatal(String[] fatal) {
		this.fatal = fatal;
	}

	public String[] getError() {
		return error;
	}

	public void setError(String[] error) {
		this.error = error;
	}

	public String[] getWarn() {
		return warn;
	}

	public void setWarn(String[] warn) {
		this.warn = warn;
	}

	public String[] getInfo() {
		return info;
	}

	public void setInfo(String[] info) {
		this.info = info;
	}

	public String[] getDebug() {
		return debug;
	}

	public void setDebug(String[] debug) {
		this.debug = debug;
	}

	public String[] getTrace() {
		return trace;
	}

	public void setTrace(String[] trace) {
		this.trace = trace;
	}

	private static final Map<String, LogLevel> loggerMap = new ConcurrentHashMap<>();

	private Set<String> noneLoggerSet = null;

	private Set<String> fatalLoggerSet = null;

	private Set<String> errorLoggerSet = null;

	private Set<String> warnLoggerSet = null;

	private Set<String> infoLoggerSet = null;

	private Set<String> debugLoggerSet = null;

	private Set<String> traceLoggerSet = null;

	private Set<String> getSet(String[] array) {
		return array == null ? Collections.emptySet() : new HashSet<>(Arrays.asList(array));
	}

	public boolean contains(Set<String> set, String methodId) {
		boolean ret = false;
		ret = set.contains(methodId);
		if (!ret) {
			for (String s : set) {
				if (s != null && methodId.startsWith(s)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public LogLevel getLogLevel(String methodId) {
		if (loggerMap.containsKey(methodId)) {
			return loggerMap.get(methodId);
		}
		if (noneLoggerSet == null) {
			noneLoggerSet = getSet(getNone());
		}
		if (fatalLoggerSet == null) {
			fatalLoggerSet = getSet(getFatal());
		}
		if (errorLoggerSet == null) {
			errorLoggerSet = getSet(getError());
		}
		if (warnLoggerSet == null) {
			warnLoggerSet = getSet(getWarn());
		}
		if (infoLoggerSet == null) {
			infoLoggerSet = getSet(getInfo());
		}
		if (debugLoggerSet == null) {
			debugLoggerSet = getSet(getDebug());
		}
		if (traceLoggerSet == null) {
			traceLoggerSet = getSet(getTrace());
		}
		if (contains(noneLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.NONE);
			return LogLevel.NONE;
		} else if (contains(fatalLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.FATAL);
			return LogLevel.FATAL;
		} else if (contains(errorLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.ERROR);
			return LogLevel.ERROR;
		} else if (contains(warnLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.WARN);
			return LogLevel.WARN;
		} else if (contains(infoLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.INFO);
			return LogLevel.INFO;
		} else if (contains(debugLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.DEBUG);
			return LogLevel.DEBUG;
		} else if (contains(traceLoggerSet, methodId)) {
			loggerMap.put(methodId, LogLevel.TRACE);
			return LogLevel.TRACE;
		}
		loggerMap.put(methodId, LogLevel.NONE);
		return LogLevel.NONE;
	}

}
