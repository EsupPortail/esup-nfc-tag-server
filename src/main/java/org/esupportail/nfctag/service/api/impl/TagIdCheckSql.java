/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.service.api.impl;

import javax.sql.DataSource;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class TagIdCheckSql implements TagIdCheckApi {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	protected JdbcTemplate jdbcTemplate;

	protected String csnAuthSql;

	protected String desfireAuthSql;
	
	protected String description;
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDataSource(DataSource datesource) {
		this.jdbcTemplate = new JdbcTemplate(datesource);
	}

	public void setCsnAuthSql(String csnAuthSql) {
		this.csnAuthSql = csnAuthSql;
	}

	public void setDesfireAuthSql(String desfireAuthSql) {
		this.desfireAuthSql = desfireAuthSql;
	}
	
	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId){
		
		String authSql = "";
		switch (tagType) {
			case CSN :
				authSql = csnAuthSql;
				break;
			case DESFIRE :
				authSql = desfireAuthSql;
				break;	
			default:
				throw new RuntimeException(tagType + " is not supported");
		}
		
		TagLog tagLog = null;
		try {
			tagLog = (TagLog) jdbcTemplate.queryForObject(authSql, new Object[] { tagId }, new TagLogRowMapper());
			log.info("tagId " + tagId + " identifié !");
		} catch (EmptyResultDataAccessException ex) {
			log.info("Pas de résultat pour le tagId : " + tagId);
		}
		return tagLog;	
	}

	@Override
	public Boolean supportTagType(TagType tagType) {
		switch (tagType) {
			case CSN :
				return csnAuthSql != null && !csnAuthSql.isEmpty();
			case DESFIRE :
				return desfireAuthSql != null && !desfireAuthSql.isEmpty();
			default:
				return false;
		}
	}

}
