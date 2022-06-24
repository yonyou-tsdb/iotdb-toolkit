package org.apache.iotdb.ui.model.exporter;

import java.util.Map;
import java.util.TreeMap;

public class ExporterBody {

	private String metricName;

	private Double value;

	private Map<String, String> label = new TreeMap<>();

	private Long timestamp;

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(Map<String, String> label) {
		this.label = label;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String buildPath() {
		if (label.isEmpty()) {
			return metricName.replaceAll(" ", "_");
		} else {
			StringBuilder sb = new StringBuilder(metricName.replaceAll(" ", "_"));
			for (Map.Entry<String, String> e : label.entrySet()) {
				sb.append(".\"").append(e.getKey()).append("=").append(e.getValue()).append("\"");
			}
			return sb.toString();
		}
	}

}
