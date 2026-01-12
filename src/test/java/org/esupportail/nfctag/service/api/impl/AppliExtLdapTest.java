package org.esupportail.nfctag.service.api.impl;

import jakarta.annotation.Resource;
import org.esupportail.nfctag.security.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class AppliExtLdapTest {

    @Autowired
    List<AppliExtLdap> appliExtLdaps;

    @Value("${test.ldap.eppn:dummy@example.org}")
    String testLdapEppn;

    @Test
    public void getLocations4EppnTest() {
        for(AppliExtLdap appliExtLdap : appliExtLdaps) {
            appliExtLdap.getLocations4Eppn(testLdapEppn);
        }
    }

}
