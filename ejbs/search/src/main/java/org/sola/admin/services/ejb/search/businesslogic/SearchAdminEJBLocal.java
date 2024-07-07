/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.admin.services.ejb.search.businesslogic;

import org.sola.admin.services.ejb.search.repository.entities.Crs;
import org.sola.admin.services.ejb.search.repository.entities.BrSearchParams;
import org.sola.admin.services.ejb.search.repository.entities.UserSearchParams;
import org.sola.admin.services.ejb.search.repository.entities.BrSearchResult;
import org.sola.admin.services.ejb.search.repository.entities.ConfigMapLayer;
import org.sola.admin.services.ejb.search.repository.entities.UserSearchResult;
import java.util.HashMap;
import java.util.List;
import jakarta.ejb.Local;
import org.sola.admin.services.ejb.search.repository.entities.ProjectSearchResult;
import org.sola.services.common.ejbs.AbstractEJBLocal;

/**
 * Local interface for the {@linkplain SearchEJB}
 */
@Local
public interface SearchAdminEJBLocal extends AbstractEJBLocal {
    /**
     * See {@linkplain SearchEJB#getConfigMapLayerList(java.lang.String)
     * SearchEJB.getConfigMapLayerList}.
     */
    List<ConfigMapLayer> getConfigMapLayerList(String languageCode);

    /**
     * See {@linkplain SearchEJB#getCrsList()
     * SearchEJB.getCrsList}.
     */
    List<Crs> getCrsList();
    
    /**
     * See {@linkplain SearchEJB#getMapSettingList()
     * SearchEJB.getMapSettingList}.
     */
    HashMap<String, String> getMapSettingList();

    /**
     * See {@linkplain SearchEJB#getActiveUsers()
     * SearchEJB.getActiveUsers}.
     */
    List<UserSearchResult> getActiveUsers();

    /**
     * See {@linkplain SearchAdminEJB#searchUsers(org.sola.services.ejb.search.repository.entities.UserSearchParams, java.lang.String)
     * SearchAdminEJB.searchUsers}.
     */
    List<UserSearchResult> searchUsers(UserSearchParams searchParams, String lang);

    /**
     * See {@linkplain SearchEJB#searchBr(org.sola.services.ejb.search.repository.entities.BrSearchParams, java.lang.String)
     * SearchEJB.searchBr}.
     */
    List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang);
    
    /**
     * See {@linkplain SearchAdminEJB#getAllProjects(String lang)}.
     */
    List<ProjectSearchResult> getAllProjects(String lang);
    
    /**
     * See {@linkplain SearchAdminEJB#getMapLayersByProject(java.lang.String, java.lang.String)
     * SearchAdminEJB.getMapLayersByProject}.
     */
    List<ConfigMapLayer> getMapLayersByProject(String projectId, String languageCode);
}
