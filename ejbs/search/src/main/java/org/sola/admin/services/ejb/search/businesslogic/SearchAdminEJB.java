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
package org.sola.admin.services.ejb.search.businesslogic;

import org.sola.admin.services.ejb.search.repository.entities.Crs;
import org.sola.admin.services.ejb.search.repository.entities.BrSearchParams;
import org.sola.admin.services.ejb.search.repository.entities.Setting;
import org.sola.admin.services.ejb.search.repository.entities.UserSearchParams;
import org.sola.admin.services.ejb.search.repository.entities.BrSearchResult;
import org.sola.admin.services.ejb.search.repository.entities.UserSearchResult;
import org.sola.admin.services.ejb.search.repository.entities.ConfigMapLayer;
import java.util.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.sola.admin.services.ejb.search.repository.entities.ProjectSearchResult;
import org.sola.common.RolesConstants;
import org.sola.common.StringUtility;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;

/**
 * SOLA EJB's have responsibility for managing data in one schema. This can
 * complicate searches that require interrogating data across multiple schemas
 * to obtain a result. If the strict rule to only allow EJBs to manage data in
 * one schema was applied, cross schema searches would require the use of
 * multiple EJBs to obtain several part datasets which would then need to be
 * sorted and filtered based on the users search criteria. That approach is very
 * inefficient compared to using SQL, so the SearchEJB has been created to allow
 * efficient searching for data across multiple schemas.
 *
 * <p>
 * The SearchEJB supports execution of dynamic SQL queries obtained from the
 * system.query table.</p>
 *
 * <p>
 * Note that this EJB has access to all SOLA database tables and it must be
 * treated as read only. It must not be used to persist data changes.</p>
 */
@Stateless
@EJB(name = "java:app/SearchAdminEJBLocal", beanInterface = SearchAdminEJBLocal.class)
public class SearchAdminEJB extends AbstractEJB implements SearchAdminEJBLocal {
    
    /**
     * Executes a search across all users using the search criteria provided.
     * Partial matches are supported for the username, first name and last name
     * criteria.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param searchParams The criteria to use for the search.
     * @return The users that match the search criteria.
     */
    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    public List<UserSearchResult> searchUsers(UserSearchParams searchParams, String lang) {
        if (searchParams.getGroupId() == null) {
            searchParams.setGroupId("");
        }

        if (searchParams.getProjectId()== null) {
            searchParams.setProjectId("");
        }
        
        if (searchParams.getUserName() == null) {
            searchParams.setUserName("");
        }

        if (searchParams.getFirstName() == null) {
            searchParams.setFirstName("");
        }

        if (searchParams.getLastName() == null) {
            searchParams.setLastName("");
        }

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ADVANCED_USER_SEARCH);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
        params.put("userName", searchParams.getUserName());
        params.put("firstName", searchParams.getFirstName());
        params.put("lastName", searchParams.getLastName());
        params.put("groupId", searchParams.getGroupId());
        params.put("projectId", searchParams.getProjectId());
        return getRepository().getEntityList(UserSearchResult.class, params);
    }

    /**
     * Returns details for all users marked as active in the SOLA database.
     */
    @Override
    public List<UserSearchResult> getActiveUsers() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ACTIVE_USERS);
        return getRepository().getEntityList(UserSearchResult.class, params);
    }

    /**
     * Returns the map layer config details from system.config_map_layer table.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ConfigMapLayer> getConfigMapLayerList(String languageCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ConfigMapLayer.QUERY_ORDER_BY);
        return getRepository().getEntityList(ConfigMapLayer.class, params);
    }

    /**
     * Returns the list of Crs
     *
     * @return
     */
    @Override
    public List<Crs> getCrsList() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, Crs.ORDER_COLUMN);
        return getRepository().getEntityList(Crs.class, params);
    }

    /**
     * Retrieves the default map settings (i.e. default extent for the map and
     * srid) from the system.settings table.
     *
     * @see #getSettingList(java.lang.String) getSettingList
     */
    @Override
    public HashMap<String, String> getMapSettingList() {
        return this.getSettingList(Setting.QUERY_SQL_FOR_MAP_SETTINGS);
    }

    /**
     * Retrieves the system settings from the system.setting table using the
     * specified query.
     *
     * @param queryBody The query to use to obtain the settings from the
     * system.setting table.
     * @see #getMapSettingList() getMapSettingList
     */
    private HashMap<String, String> getSettingList(String queryBody) {
        HashMap settingMap = new HashMap();
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, queryBody);
        List<Setting> settings = getRepository().getEntityList(Setting.class, params);
        if (settings != null && !settings.isEmpty()) {
            for (Setting setting : settings) {
                settingMap.put(setting.getId(), setting.getVl());
            }
        }
        return settingMap;
    }

    /**
     * Executes a search across all Business Rules. Partial matches of the br
     * display name are supported.
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_BR} role.</p>
     *
     * @param searchParams The parameters to use for the search.
     * @param lang The language code to use for localization of display values
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang) {
        Map params = new HashMap<String, Object>();

        if (searchParams.getDisplayName() == null) {
            searchParams.setDisplayName("");
        }
        if (searchParams.getTargetCode() == null) {
            searchParams.setTargetCode("");
        }
        if (searchParams.getTechnicalTypeCode() == null) {
            searchParams.setTechnicalTypeCode("");
        }

        searchParams.setDisplayName(searchParams.getDisplayName().trim());

        params.put(CommonSqlProvider.PARAM_QUERY, BrSearchResult.SELECT_QUERY);
        params.put("lang", lang);
        params.put("displayName", searchParams.getDisplayName());
        params.put("technicalTypeCode", searchParams.getTechnicalTypeCode());
        params.put("targetCode", searchParams.getTargetCode());
        return getRepository().getEntityList(BrSearchResult.class, params);
    }
    
    /**
     * Returns list of all projects
     *
     * @param lang Language code
     * @return
     */
    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    public List<ProjectSearchResult> getAllProjects(String lang) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
        return getRepository().getEntityList(ProjectSearchResult.class, params);
    }
    
    @Override
    public List<ConfigMapLayer> getMapLayersByProject(String projectId, String languageCode){
        if(StringUtility.isEmpty(projectId)){
            return null;
        }
        
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ConfigMapLayer.WHERE_BY_PROJECT_ID);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(ConfigMapLayer.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntityList(ConfigMapLayer.class, params);
    }
}
