/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.admin.services.ejb.system.businesslogic;

import org.sola.admin.services.ejb.system.repository.entities.Br;
import org.sola.admin.services.ejb.system.repository.entities.BrCurrent;
import org.sola.admin.services.ejb.system.repository.entities.EmailTask;
import org.sola.admin.services.ejb.system.repository.entities.Query;
import org.sola.admin.services.ejb.system.repository.entities.BrReport;
import org.sola.admin.services.ejb.system.repository.entities.Setting;
import org.sola.admin.services.ejb.system.repository.entities.BrValidation;
import org.sola.admin.services.ejb.system.repository.entities.Crs;
import org.sola.admin.services.ejb.system.repository.entities.DbInfo;
import org.sola.admin.services.ejb.system.repository.entities.BrDefinition;
import org.sola.admin.services.ejb.system.repository.entities.ConfigMapLayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.ConfigConstants;
import org.sola.common.RolesConstants;
import org.sola.common.SOLAException;
import org.sola.common.StringUtility;
import org.sola.common.messaging.ServiceMessage;
import org.sola.services.common.EntityAction;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;

/**
 * System EJB - Provides access to SOLA System data including business rules
 */
@Stateless
@EJB(name = "java:app/SystemAdminEJBLocal", beanInterface = SystemAdminEJBLocal.class)
public class SystemAdminEJB extends AbstractEJB implements SystemAdminEJBLocal {

    /**
     * Sets the entity package for the EJB to Br.class.getPackage().getName().
     * This is used to restrict the save and retrieval of Code Entities.
     *
     * @see AbstractEJB#getCodeEntity(java.lang.Class, java.lang.String,
     * java.lang.String) AbstractEJB.getCodeEntity
     * @see AbstractEJB#getCodeEntityList(java.lang.Class, java.lang.String)
     * AbstractEJB.getCodeEntityList
     * @see
     * AbstractEJB#saveCodeEntity(org.sola.services.common.repository.entities.AbstractCodeEntity)
     * AbstractEJB.saveCodeEntity
     */
    @Override
    protected void postConstruct() {
        setEntityPackage(Br.class.getPackage().getName());
    }

    /**
     * Returns all configuration settings in the system.setting table.
     */
    @Override
    public List<Setting> getAllSettings() {
        return getRepository().getEntityList(Setting.class);
    }

    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    public Setting saveSetting(Setting setting) {
        return getRepository().saveEntity(setting);
    }

    /**
     * Retrieves the value for the named setting. Constants for each setting are
     * available in {@linkplain  ConfigConstants}. If the setting does not exist,
     * the default value for the setting is returned.
     *
     * @param name The name of the setting to retrieve
     * @param defaultValue The default value for the setting if it no override
     * value is recorded in the system.settings table.
     * @return The override value for the setting or the defaultValue.
     */
    @Override
    public String getSetting(String name, String defaultValue) {
        String result = defaultValue;
        Setting config = getSetting(name);
        if (config != null && config.getValue() != null) {
            result = config.getValue();
        }
        return result;
    }

    @Override
    public Setting getSetting(String name) {
        Setting result = null;
        // Use getAllSettings to obtain the cached settings. 
        List<Setting> settings = getAllSettings();
        for (Setting config : settings) {
            if (config.getName().equals(name)) {
                result = config;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the SOLA business rule matching the id.
     *
     * <p>
     * Requires the {@linkplain RolesConstants.ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param id Identifier for the business rule to return
     * @param lang The language code to use to localize the display value for
     * each Br.
     *
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public Br getBr(String id, String lang) {
        if (lang == null) {
            return getRepository().getEntity(Br.class, id);
        } else {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
            return getRepository().getEntity(Br.class, id, lang);
        }
    }

    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    public boolean deleteBr(String brId) {
        if (StringUtility.isEmpty(brId)) {
            return true;
        }
        Br br = getBr(brId, null);
        if (br != null) {
            // Delete validations
            if (br.getBrValidationList() != null) {
                for (BrValidation validation : br.getBrValidationList()) {
                    validation.setEntityAction(EntityAction.DELETE);
                    getRepository().saveEntity(validation);
                }
            }
            // Delete definitions
            if (br.getBrDefinitionList() != null) {
                for (BrDefinition def : br.getBrDefinitionList()) {
                    def.setEntityAction(EntityAction.DELETE);
                    getRepository().saveEntity(def);
                }
            }
            br.setEntityAction(EntityAction.DELETE);
            getRepository().saveEntity(br);
        }
        return true;
    }

    /**
     * Can be used to create a new business rule or save any updates to the
     * details of an existing business role.
     *
     * <p>
     * Requires the {@linkplain RolesConstants.ADMIN_MANAGE_SECURITY} role. </p>
     *
     * @param br The business rule to save.
     * @return The updated/new business rule.
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public Br saveBr(Br br) {
        return getRepository().saveEntity(br);
    }

    /**
     * Retrieves the br specified by the id from the system.br_current view. The
     * view lists all br's that are currently active.
     *
     * @param id The identifier of the br to retrieve.
     * @param languageCode The language code to localize the display values and
     * validation messages for the business rule.
     * @throws SOLAException If the business rule is not found
     */
    private BrCurrent getBrCurrent(String id, String languageCode) {
        BrCurrent result = null;
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrCurrent.QUERY_WHERE_BYID);
        params.put(BrCurrent.QUERY_PARAMETER_ID, id);
        result = getRepository().getEntity(BrCurrent.class, params);
        if (result == null) {
            throw new SOLAException(ServiceMessage.RULE_NOT_FOUND, new Object[]{id});
        }
        return result;
    }

    /**
     * Returns the Br Report for the specified business rule.
     *
     * @param id Identifer of the business rule to retrieve the report for.
     */
    @Override
    public BrReport getBrReport(String id) {
        Map params = new HashMap<String, Object>();
        return getRepository().getEntity(BrReport.class, id);
    }

    /**
     * Returns a list of business rules matching the supplied ids.
     *
     * <p>
     * No role is required to execute this method.</p>
     *
     * @param ids The list of business rule ids
     */
    @Override
    public List<BrReport> getBrs(List<String> ids) {
        Map params = new HashMap<String, Object>();
        return getRepository().getEntityListByIds(BrReport.class, ids);
    }

    /**
     * Returns a br report for every business rule in the system.br table.
     *
     * <p>
     * No role is required to execute this method.</p>
     */
    @Override
    public List<BrReport> getAllBrs() {
        return getRepository().getEntityList(BrReport.class);
    }

    /**
     * Returns all emails that need to be send at the current time.
     */
    @Override
    public List<EmailTask> getEmailsToSend() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, EmailTask.WHERE_BY_NOW);
        return getRepository().getEntityList(EmailTask.class, params);
    }

    /**
     * Returns all email tasks
     */
    @Override
    public List<EmailTask> getEmails() {
        return getRepository().getEntityList(EmailTask.class);
    }

    /**
     * Saves email task.
     *
     * @param emailTask Email task to save
     * @return
     */
    @Override
    public EmailTask saveEmailTask(EmailTask emailTask) {
        return getRepository().saveEntity(emailTask);
    }

    /**
     * Saves email task.
     *
     * @param id Email task ID
     * @return
     */
    @Override
    public EmailTask getEmailTask(String id) {
        return getRepository().getEntity(EmailTask.class, id);
    }

    /**
     * Returns true if email service is enabled on the system, otherwise false.
     */
    @Override
    public boolean isEmailServiceEnabled() {
        return getSetting(ConfigConstants.EMAIL_ENABLE_SERVICE, "0").equals("1");
    }

    /**
     * Send simple email to the given address
     *
     * @param recipientName Recipient name (full name)
     * @param recipientAddress Recipient email address
     * @param subject Subject of the message
     * @param body Message text
     */
    @Override
    public void sendEmail(String recipientName, String recipientAddress, String body, String subject) {
        EmailTask task = new EmailTask();
        task.setId(UUID.randomUUID().toString());
        task.setBody(body);
        task.setRecipient(recipientAddress);
        task.setRecipientName(recipientName);
        task.setSubject(subject);
        saveEmailTask(task);
    }

    /**
     * Returns list of available CRS
     *
     * @return
     */
    @Override
    public List<Crs> getCrss() {
        return getRepository().getEntityList(Crs.class);
    }

    /**
     * Returns CRS by provided srid
     *
     * @param srid srid of CRS
     * @return
     */
    @Override
    public Crs getCrs(int srid) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, "srid=" + srid);
        return getRepository().getEntity(Crs.class, params);
    }

    /**
     * Saves provided CRS
     *
     * @param crs CRS object to save
     * @return
     */
    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    public Crs saveCrs(Crs crs) {
        return getRepository().saveEntity(crs);
    }

    /**
     * Returns list of layer queries
     *
     * @param locale Locale code
     * @return
     */
    @Override
    public List<Query> getQueries(String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            return getRepository().getEntityList(Query.class, params);
        }
        return getRepository().getEntityList(Query.class);
    }

    /**
     * Returns layer query
     *
     * @param name Query name
     * @param locale Locale code
     * @return
     */
    @Override
    public Query getQuery(String name, String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            params.put(CommonSqlProvider.PARAM_WHERE_PART, "name='" + name + "'");
            return getRepository().getEntity(Query.class, params);
        }
        return getRepository().getEntity(Query.class, name);
    }

    /**
     * Saves layer query
     *
     * @param query Query object to save
     * @return
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public Query saveQuery(Query query) {
        return getRepository().saveEntity(query);
    }

    /**
     * Returns list of map layers
     *
     * @param locale Locale code
     * @return
     */
    @Override
    public List<ConfigMapLayer> getConfigMapLayers(String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            return getRepository().getEntityList(ConfigMapLayer.class, params);
        }
        return getRepository().getEntityList(ConfigMapLayer.class);
    }

    /**
     * Returns map layer
     *
     * @param name Layer name
     * @param locale Locale code
     * @return
     */
    @Override
    public ConfigMapLayer getConfigMapLayer(String name, String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            params.put(CommonSqlProvider.PARAM_WHERE_PART, "name='" + name + "'");
            return getRepository().getEntity(ConfigMapLayer.class, params);
        }
        return getRepository().getEntity(ConfigMapLayer.class, name);
    }

    /**
     * Saves map layer
     *
     * @param mapLayer Map layer to save
     * @return
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public ConfigMapLayer saveConfigMapLayer(ConfigMapLayer mapLayer) {
        return getRepository().saveEntity(mapLayer);
    }

    /**
     * @return Connected database info.
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public DbInfo getDatabaseInfo() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, DbInfo.QUERY);
        return getRepository().getEntity(DbInfo.class, params);
    }
}
