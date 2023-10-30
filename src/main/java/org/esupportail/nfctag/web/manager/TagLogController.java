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

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.TagLogDao;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/manager/taglogs")
@Controller
public class TagLogController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ApplicationDao applicationDao;

    @Resource
    private TagLogDao tagLogDao;

	List<String> listSearchBy = Arrays.asList("authDate", "applicationName", "location", "eppnInit", "numeroId", "csn", "desfireId");

    @RequestMapping(produces = "text/html")
    public String list(
    		@RequestParam(required = false, defaultValue = "") String searchString,
    		@RequestParam(required = false, defaultValue = "") String applicationFilter,
    		@RequestParam(required = false, defaultValue = "") String statusFilter,
    		@RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "authDate") String sortFieldName,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            Model uiModel) {
          
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
      
        List<TagLog> taglogs = tagLogDao.findTagLogs(searchString, statusFilter, applicationFilter, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList();
        float nrOfPages = (float) tagLogDao.countFindTagLogs(searchString, statusFilter, applicationFilter) / sizeNo;
        
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        uiModel.addAttribute("applications", tagLogDao.findApplications());
        uiModel.addAttribute("status", TagLog.Status.values());
        uiModel.addAttribute("page", page);
        uiModel.addAttribute("size", size);
    	uiModel.addAttribute("taglogs", taglogs);
    	uiModel.addAttribute("searchString", searchString);
        uiModel.addAttribute("applicationFilter", applicationFilter);
        uiModel.addAttribute("statusFilter", statusFilter);
        uiModel.addAttribute("listSearchBy", listSearchBy);
        uiModel.addAttribute("queryUrl", "?statusFilter="+statusFilter+"&applicationFilter="+applicationFilter+"&searchString="+searchString);
        addDateTimeFormatPatterns(uiModel);
        return "manager/taglogs/list";
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("taglog", tagLogDao.findTagLog(id));
        uiModel.addAttribute("itemId", id);
        return "manager/taglogs/show";
    }

    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("tagLog_authdate_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
    }
}
