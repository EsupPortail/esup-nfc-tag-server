package org.esupportail.nfctag.service.api.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.esupportail.nfctag.domain.TagLog;
import org.springframework.jdbc.core.RowMapper;

public class TagLogRowMapper implements RowMapper<TagLog>
{
	public TagLog mapRow(ResultSet rs, int rowNum) throws SQLException {
		TagLog tagLog = new TagLog();
		tagLog.setCsn(rs.getString("csn"));
		tagLog.setEppn(rs.getString("eppn"));
		tagLog.setFirstname(rs.getString("nom"));
		tagLog.setLastname(rs.getString("prenom"));
		return tagLog;
	}
	
}
