package org.esupportail.nfctag.security;

import java.util.List;

public interface GroupService {

	List<String> getGroups(String eppn);

	List<String> getMembers(String groupName);

}