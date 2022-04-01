package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.EmailLog;

public interface EmailLogDao {

	public int insert(EmailLog t);

	public EmailLog select(Long id);

	public List<EmailLog> selectAll(EmailLog t);

	public EmailLog selectOne(EmailLog t);

	public int update(EmailLog t);

	public int updatePersistent(EmailLog t);

	public int delete(EmailLog t);

	public int count(EmailLog t);

}
