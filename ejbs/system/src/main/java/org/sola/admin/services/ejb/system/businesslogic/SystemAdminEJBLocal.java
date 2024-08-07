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
package org.sola.admin.services.ejb.system.businesslogic;

import java.util.List;
import jakarta.ejb.Local;
import org.sola.services.common.ejbs.AbstractEJBLocal;
import org.sola.admin.services.ejb.system.repository.entities.Br;
import org.sola.admin.services.ejb.system.repository.entities.BrReport;
import org.sola.admin.services.ejb.system.repository.entities.ConfigMapLayer;
import org.sola.admin.services.ejb.system.repository.entities.Crs;
import org.sola.admin.services.ejb.system.repository.entities.DbInfo;
import org.sola.admin.services.ejb.system.repository.entities.EmailTask;
import org.sola.admin.services.ejb.system.repository.entities.Project;
import org.sola.admin.services.ejb.system.repository.entities.Query;
import org.sola.admin.services.ejb.system.repository.entities.ReportDescription;
import org.sola.admin.services.ejb.system.repository.entities.Setting;

/**
 * The EJB local interface for the {@linkplain SystemEJB}. The SystemEJB provides access to SOLA
 * System data including business rules.
 */
@Local
public interface SystemAdminEJBLocal extends AbstractEJBLocal {
    /**
     * See {@linkplain SystemEJB#getAllSettings() SystemEJB.getAllSettings}
     */
    List<Setting> getAllSettings();

    /**
     * See {@linkplain SystemEJB#getSetting(java.lang.String, java.lang.String)  SystemEJB.getSetting}
     */
    String getSetting(String name, String defaultValue);
    
    /** 
     * Returns setting object, by given name
     * @param name Setting name
     * @return 
     */
    Setting getSetting(String name);
    
    /** 
     * Saves system setting
     * @param setting Setting object to save
     * @return  
     */
    Setting saveSetting(Setting setting);

    /**
     * See {@linkplain org.sola.services.ejb.system.businesslogic.SystemEJB#getBr(java.lang.String, java.lang.String)
     * SystemEJB.getBr}
     */
    Br getBr(String id, String lang);

    /**
     * See {@linkplain org.sola.services.ejb.system.businesslogic.SystemEJB#saveBr(org.sola.services.ejb.system.repository.entities.Br)
     * SystemEJB.saveBr}
     */
    Br saveBr(Br br);
    
    boolean deleteBr(String brId);

    /**
     * See {@linkplain SystemEJB#getBrs(java.util.List)
     * SystemEJB.getBrs
     */
    List<BrReport> getBrs(List<String> ids);

    /**
     * See {@linkplain SystemEJB#getAllBrs()
     * SystemEJB.getAllBrs
     */
    List<BrReport> getAllBrs();

    /**
     * See {@linkplain SystemEJB#getBrReport(java.lang.String)
     * SystemEJB.getBrReport
     */
    BrReport getBrReport(String id);

    /**
     * See {@linkplain SystemEJB#getEmailsToSend()
     * SystemEJB.getEmailsToSend
     */
    List<EmailTask> getEmailsToSend();
    
    /**
     * See {@linkplain SystemEJB#getEmails()
     * SystemEJB.getEmails
     */
    List<EmailTask> getEmails();
    
    /**
     * See {@linkplain SystemEJB#saveEmailTask(EmailTask)
     * SystemEJB.saveEmailTask
     */
    EmailTask saveEmailTask(EmailTask emailTask);
    
    /**
     * See {@linkplain SystemEJB#getEmailTask(String)
     * SystemEJB.getEmailTask
     */
    EmailTask getEmailTask(String id);
    
    /** See {@link SystemEJB#sendEmail(String, String, String, String) }*/
    void sendEmail(String recipientName, String recipientAddress, String body, String subject);
    
    /** See {@link SystemEJB#isEmailServiceEnabled() }*/
    boolean isEmailServiceEnabled();
    
    /** See {@link SystemEJB#getCrss()}*/
    List<Crs> getCrss();
    
    /** See {@link SystemEJB#getCrs(int srid)}*/
    Crs getCrs(int srid);
    
    /** See {@link SystemEJB#saveCrs(Crs crs)}*/
    Crs saveCrs(Crs crs);
    
    /** See {@link SystemEJB#getQueries(String locale)}*/
    List<Query> getQueries(String locale);
    
    /** See {@link SystemEJB#getQuery(String name, String locale)}*/
    Query getQuery(String name, String locale);
    
    /** See {@link SystemEJB#saveQuery(Query query)}*/
    Query saveQuery(Query query);
    
    /** See {@link SystemEJB#getConfigMapLayers(String locale)}*/
    List<ConfigMapLayer> getConfigMapLayers(String locale);
    
    /** See {@link SystemEJB#getConfigMapLayer(String name, String locale)}*/
    ConfigMapLayer getConfigMapLayer(String name, String locale);
    
    /** See {@link SystemEJB#saveConfigMapLayer(ConfigMapLayer mapLayer)}*/
    ConfigMapLayer saveConfigMapLayer(ConfigMapLayer mapLayer);
    
    /** See {@link SystemEJB#getDatabaseInfo()}*/
    DbInfo getDatabaseInfo();
    
    /**
     * See {@linkplain SystemAdminEJB#getAllReports(java.lang.String)
     * SystemAdminEJB.getAllReports}
     * @param locale
     * @return 
     */
    List<ReportDescription> getAllReports(String locale);

    /**
     * See {@linkplain SystemAdminEJB#getReportById(java.lang.String, java.lang.String)
     * SystemAdminEJB.getReportById}
     */
    ReportDescription getReportById(String id, String locale);
    
    /**
     * See {@linkplain SystemAdminEJB#saveReport(org.sola.admin.services.ejb.system.repository.entities.ReportDescription)
     * SystemAdminEJB.saveReport}
     */
    ReportDescription saveReport(ReportDescription report);
    
    /**
     * Saves project
     *
     * @param project Project object to save
     * @return
     */
    Project saveProject(Project project);
}