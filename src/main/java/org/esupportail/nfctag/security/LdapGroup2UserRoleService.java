package org.esupportail.nfctag.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LdapGroup2UserRoleService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	protected GroupService groupService;

	protected Map<String, String> mappingGroupesRoles;
	
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}
	
	public Set<String> getRoles(String eppn) {
		Set<String> roles = new HashSet<String>();
		for(String groupName : groupService.getGroups(eppn)) {
			if(mappingGroupesRoles.containsKey(groupName)) {
				String role = mappingGroupesRoles.get(groupName);
				roles.add(role);
			}
		}
		return roles;
	}
	
}
