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
package org.sola.admin.services.ejb.search.repository.entities;

import jakarta.persistence.Column;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

@Table(name = "appuser", schema = "system")
public class UserSearchResult extends AbstractReadOnlyEntity {

    protected static final String SELECT_QUERY =
            "SELECT DISTINCT u.id, u.username, u.active, u.first_name, u.last_name, u.description, "
            + "(SELECT string_agg(tmp.name, ', ') FROM "
            + "(SELECT name FROM system.appgroup g INNER JOIN system.appuser_appgroup ug2 "
            + "ON g.id = ug2.appgroup_id WHERE ug2.appuser_id = u.id ORDER BY g.name) tmp "
            + ") AS groups_list, "
            + "(SELECT string_agg(tmp2.display_name, ', ') FROM "
            + "(SELECT get_translation(p.display_name, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as display_name FROM system.project p INNER JOIN system.project_appuser up2 "
            + "ON p.id = up2.project_id WHERE up2.appuser_id = u.id ORDER BY p.display_name) tmp2 "
            + ") AS projects_list "
            + "FROM (system.appuser u LEFT JOIN system.appuser_appgroup ug ON u.id = ug.appuser_id) LEFT JOIN system.project_appuser up on u.id = up.appuser_id ";
    
    public static final String QUERY_ACTIVE_USERS = UserSearchResult.SELECT_QUERY 
            + "WHERE active = 't' ORDER BY u.last_name";
    
    public static final String QUERY_ADVANCED_USER_SEARCH = UserSearchResult.SELECT_QUERY
            + "WHERE POSITION(LOWER(COALESCE(#{userName}, '')) IN LOWER(COALESCE(username, ''))) > 0 "
            + "AND POSITION(LOWER(COALESCE(#{firstName}, '')) IN LOWER(COALESCE(first_name, ''))) > 0 "
            + "AND POSITION(LOWER(COALESCE(#{lastName}, '')) IN LOWER(COALESCE(last_name, ''))) > 0 "
            + "AND (up.project_id = #{projectId} OR #{projectId} = '') "
            + "AND (ug.appgroup_id = #{groupId} OR #{groupId} = '') ORDER BY u.username";
    
    @Id
    @Column(name = "id")
    String id;
    @Column(name = "username")
    private String userName;
    @Column(name = "active")
    private boolean active;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "description")
    private String description;
    @Column(name = "groups_list")
    private String groupsList;
    @Column(name = "projects_list")
    private String projectsList;

    public UserSearchResult() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupsList() {
        return groupsList;
    }

    public void setGroupsList(String groupsList) {
        this.groupsList = groupsList;
    }

    public String getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(String projectsList) {
        this.projectsList = projectsList;
    }
}
