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
package org.esupportail.nfctag.web;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.dao.TagLogDao;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

import javax.annotation.Resource;

/**
 * A central place to register application converters and formatters. 
 */
@Configurable
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

    @Resource
    private ApplicationDao applicationDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private TagLogDao tagLogDao;

    public Converter<Long, Device> getIdToDeviceConverter() {
        return new Converter<Long, Device>() {
            public Device convert(Long id) {
                return deviceDao.findDevice(id);
            }
        };
    }

    public Converter<String, Application> getStringToApplicationConverter() {
        return new Converter<String, Application>() {
            public Application convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Application.class);
            }
        };
    }

    public Converter<Application, String> getApplicationToStringConverter() {
        return new Converter<Application, String>() {
            public String convert(Application application) {
                return new StringBuilder().append(application.getName()).append(' ').append(application.getNfcConfig()).append(' ').append(application.getAppliExt()).append(' ').append(application.getTagIdCheck()).toString();
            }
        };
    }

    public Converter<Long, Application> getIdToApplicationConverter() {
        return new Converter<Long, Application>() {
            public Application convert(Long id) {
                return applicationDao.findApplication(id);
            }
        };
    }

    public Converter<Device, String> getDeviceToStringConverter() {
        return new Converter<Device, String>() {
            public String convert(Device device) {
                return new StringBuilder().append(device.getNumeroId()).append(' ').append(device.getEppnInit()).append(' ').append(device.getImei()).append(' ').append(device.getMacAddress()).toString();
            }
        };
    }

    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }

    public Converter<String, Device> getStringToDeviceConverter() {
        return new Converter<String, Device>() {
            public Device convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Device.class);
            }
        };
    }

    public Converter<TagLog, String> getTagLogToStringConverter() {
        return new Converter<TagLog, String>() {
            public String convert(TagLog tagLog) {
                return new StringBuilder().append(tagLog.getStatus()).append(' ').append(tagLog.getLiveStatus()).append(' ').append(tagLog.getDesfireId()).append(' ').append(tagLog.getCsn()).toString();
            }
        };
    }

    public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getApplicationToStringConverter());
        registry.addConverter(getIdToApplicationConverter());
        registry.addConverter(getStringToApplicationConverter());
        registry.addConverter(getDeviceToStringConverter());
        registry.addConverter(getIdToDeviceConverter());
        registry.addConverter(getStringToDeviceConverter());
        registry.addConverter(getTagLogToStringConverter());
        registry.addConverter(getIdToTagLogConverter());
        registry.addConverter(getStringToTagLogConverter());
    }

    public Converter<Long, TagLog> getIdToTagLogConverter() {
        return new Converter<Long, TagLog>() {
            public TagLog convert(Long id) {
                return tagLogDao.findTagLog(id);
            }
        };
    }

    public Converter<String, TagLog> getStringToTagLogConverter() {
        return new Converter<String, TagLog>() {
            public TagLog convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), TagLog.class);
            }
        };
    }
}
