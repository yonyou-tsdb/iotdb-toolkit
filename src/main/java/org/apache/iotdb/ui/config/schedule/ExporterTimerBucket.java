package org.apache.iotdb.ui.config.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.iotdb.ui.entity.Exporter;

public class ExporterTimerBucket {

	private Map<Object, Exporter> exporterTimerMap = new ConcurrentHashMap<>(8);

	public Map<Object, Exporter> getExporterTimerMap() {
		return exporterTimerMap;
	}

	public Exporter getExporterTimer(Object name) {
		return exporterTimerMap.get(name);
	}

	public void addExporterTimer(Exporter exporter) {
		exporterTimerMap.put(exporter.getId(), exporter);
	}

	public void removeExporterTimer(Object key) {
		exporterTimerMap.remove(key);
	}
}
