package org.esupportail.nfctag.security;

import org.esupportail.nfctag.domain.NfcHttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class NfcHttpSessionsListenerService {

    Logger log = LoggerFactory.getLogger(NfcHttpSessionsListenerService.class);

    Map<String, NfcHttpSession> sessions = new HashMap<>();

    @EventListener
    public void onHttpSessionCreatedEvent(HttpSessionCreatedEvent event) {
        String id = event.getSession().getId();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String remoteIp = request.getRemoteAddr();
        String originRequestUri = request.getRequestURI();
        Date createdDate = new Date();
        NfcHttpSession session = new NfcHttpSession();
        session.setSessionId(id);
        session.setRemoteIp(remoteIp);
        session.setCreatedDate(createdDate);
        session.setOriginRequestUri(originRequestUri);
        sessions.put(id, session);
    }

    @EventListener
    public void onHttpSessionDestroyedEvent(HttpSessionDestroyedEvent event) {
        sessions.remove(event.getSession().getId());
    }

    public Map<String, NfcHttpSession> getSessions() {
        return sessions;
    }

}
