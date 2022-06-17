package org.apache.iotdb.ui.model;

import java.util.List;

import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

public class ExporterInsert {

	private List<String> measurements;

	private List<TSDataType> types;

	List<Object> values;

}
