package org.apache.iotdb.ui.model.exporter;

import java.util.Map;
import java.util.TreeMap;

public class ExporterBody {

	private String metricName;

	private Object value;

	private Map<String, String> label;

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Map<String, String> getLabel() {
		if (label == null) {
			label = new TreeMap<>();
		}
		return label;
	}

	public void setLabel(Map<String, String> label) {
		this.label = label;
	}

}
