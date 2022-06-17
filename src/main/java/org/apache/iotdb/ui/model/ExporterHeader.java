package org.apache.iotdb.ui.model;

public class ExporterHeader {

	public ExporterHeader() {
		type = ExporterMessageType.UNTYPE;
	}

	private ExporterMessageType type;

	private String metricName;

	public ExporterMessageType getType() {
		return type;
	}

	public void setType(ExporterMessageType type) {
		this.type = type;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

}
