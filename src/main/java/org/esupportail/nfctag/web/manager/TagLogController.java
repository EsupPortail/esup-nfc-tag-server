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

import jakarta.annotation.Resource;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.TagLogDao;
import org.esupportail.nfctag.domain.TagLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "taglogs";
    }

    @RequestMapping(produces = "text/html")
    public String list(
    		@RequestParam(required = false, defaultValue = "") String searchString,
    		@RequestParam(required = false, defaultValue = "") String applicationFilter,
    		@RequestParam(required = false, defaultValue = "") String statusFilter,
            @PageableDefault(size = 10) Pageable pageable,
            Model uiModel) {

        Page<TagLog> taglogs = tagLogDao.findTagLogs(searchString, statusFilter, applicationFilter, pageable);

        uiModel.addAttribute("applications", tagLogDao.findApplications());
        List<String> statuses = Arrays.stream(TagLog.Status.values()).map(Enum::name).toList();
        uiModel.addAttribute("status", statuses);
    	uiModel.addAttribute("taglogs", taglogs);
    	uiModel.addAttribute("searchString", searchString);
        uiModel.addAttribute("applicationFilter", applicationFilter);
        uiModel.addAttribute("statusFilter", statusFilter);
        addDateTimeFormatPatterns(uiModel);

        return "templates/manager/taglogs/list";
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("taglog", tagLogDao.findTagLog(id));
        uiModel.addAttribute("itemId", id);
        return "templates/manager/taglogs/show";
    }

    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("tagLog_authdate_date_format", "dd/MM/yyyy HH:mm:ss");
    }
}
