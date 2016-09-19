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
package org.esupportail.nfctag.web.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.esupportail.nfctag.domain.TagLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/taglogs")
@Controller
@RooWebScaffold(path = "manager/taglogs", formBackingObject = TagLog.class, create=false, delete=false, update=false)
public class TagLogController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
	List<String> listSearchBy = Arrays.asList("authDate", "applicationName", "location", "eppnInit", "numeroId", "csn", "desfireId");
	
    @RequestMapping(produces = "text/html")
    public String list(
    		@RequestParam(value = "searchBySelected", required = false) String searchBySelected,
    		@RequestParam(value = "searchString", required = false) String searchString,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

    	List<TagLog> taglogs = new ArrayList<TagLog>();
    	
    	if ("authDate".equals(searchBySelected)) {
			try {
	    		DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
				Date dateBegin = format.parse(searchString);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateBegin);
				calendar.add(Calendar.DATE, 1);
				Date dateEnd = calendar.getTime();
	    		taglogs = TagLog.findTagLogsByAuthDateBetween(dateBegin, dateEnd).getResultList();
			} catch (ParseException e) {
				log.warn("Unparseable date : " + searchString);
			}

    	} else if ("applicationName".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByApplicationNameEquals(searchString).getResultList();
    	} else if ("location".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByLocationEquals(searchString).getResultList();
    	} else if ("eppnInit".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByEppnInitLike(searchString).getResultList();
    	} else if ("numeroId".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByNumeroIdEquals(searchString).getResultList();
    	} else if ("csn".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByCsnEquals(searchString).getResultList();
    	} else if ("desfireId".equals(searchBySelected)) {
    		taglogs = TagLog.findTagLogsByDesfireIdEquals(searchString).getResultList();
    	} else {
	    	if(sortFieldName == null){
	    		sortFieldName = "authDate";
	    		sortOrder = "DESC";
	    	}
	        if (page != null || size != null) {
	            int sizeNo = size == null ? 10 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            taglogs = TagLog.findTagLogEntries(firstResult, sizeNo, sortFieldName, sortOrder);
	            float nrOfPages = (float) TagLog.countTagLogs() / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            taglogs = TagLog.findAllTagLogs(sortFieldName, sortOrder);
	        }
    	}

    	uiModel.addAttribute("taglogs", taglogs);
    	uiModel.addAttribute("searchString", searchString);
        uiModel.addAttribute("searchBySelected", searchBySelected);
        uiModel.addAttribute("listSearchBy", listSearchBy);
        addDateTimeFormatPatterns(uiModel);
        return "manager/taglogs/list";
    }
    
}
