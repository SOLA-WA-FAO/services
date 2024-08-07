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
package org.sola.admin.services.ejbs.admin.businesslogic;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.Configuration;
import org.sola.common.ConfigConstants;
import org.sola.common.DateUtility;
import org.sola.common.EmailVariables;
import org.sola.common.FileUtility;
import org.sola.common.RolesConstants;
import org.sola.common.StringUtility;
import org.sola.services.common.EntityAction;
import org.sola.services.common.EntityTable;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.admin.services.ejb.system.businesslogic.SystemAdminEJBLocal;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.GroupSummary;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.Language;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.Role;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.User;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.Group;
import org.sola.admin.services.ejbs.admin.businesslogic.repository.entities.UserGroup;

/**
 * Contains business logic methods to administer system settings, users and
 * roles.
 */
@Stateless
@EJB(name = "java:app/AdministratorEJBLocal", beanInterface = AdministratorEJBLocal.class)
public class AdministratorEJB extends AbstractEJB implements AdministratorEJBLocal {

    @EJB
    private SystemAdminEJBLocal systemEJB;

    /**
     * Returns the list of all users from the database.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public List<User> getUsers() {
        return getRepository().getEntityList(User.class);
    }

    /**
     * Returns database configuration related to the EJB.
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public Configuration getDbConfiguration() {
        return getRepository().getDbConnectionManager().getSqlSessionFactory().getConfiguration();
    }

    /**
     * Returns the details of the user with the specified user name.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param userName The user name of the user to search for.
     */
    @RolesAllowed({RolesConstants.ADMIN_MANAGE_SECURITY, RolesConstants.ADMIN_CHANGE_PASSWORD})
    @Override
    public User getUser(String userName) {
        return getUserInfo(userName);
    }

    /**
     * Returns full user's name (first and last name)
     *
     * @param userName User name (login)
     * @return
     */
    @Override
    public String getUserFullName(String userName) {
        User user = getUserInfo(userName);
        String fullName = "";

        if (user == null) {
            return "";
        }

        if (user.getFirstName() != null) {
            fullName = user.getFirstName();
        }

        if (user.getLastName() != null) {
            if (fullName.length() > 0) {
                fullName += " " + user.getLastName();
            } else {
                fullName = user.getLastName();
            }
        }
        return fullName;
    }

    /**
     * Returns the details of the user with the specified user name. Should be
     * used only between EJBs, not exposing this method outside. It has no
     * security roles.
     *
     * @param userName The user name of the user to search for.
     * @return
     */
    @Override
    public User getUserInfo(String userName) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_USERNAME);
        params.put(User.PARAM_USERNAME, userName);
        return getRepository().getEntity(User.class, params);
    }

    /**
     * Returns true is user name exists, otherwise false
     *
     * @param userName User name to check
     * @return
     */
    @Override
    public boolean isUserNameExists(String userName) {
        User user = getUserInfo(userName);
        return user != null && user.getUserName() != null && !user.getUserName().equals("");
    }

    /**
     * Returns true is email exists, otherwise false
     *
     * @param email Email address to check
     * @return
     */
    @Override
    public boolean isUserEmailExists(String email) {
        User user = getUserByEmail(email);
        return user != null && user.getEmail() != null && !user.getEmail().equals("");
    }

    private User getUserByEmail(String email) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_EMAIL);
        params.put(User.PARAM_EMAIL, email);
        return getRepository().getEntity(User.class, params);
    }

    /**
     * Returns true is email exists, excluding provided user name, otherwise
     * false
     *
     * @param email Email address to check
     * @return
     */
    @Override
    public boolean isUserEmailExists(String email, String exludeUserName) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_EMAIL_EXCLUDE_USERNAME);
        params.put(User.PARAM_EMAIL, email);
        params.put(User.PARAM_USERNAME, exludeUserName);
        User user = getRepository().getEntity(User.class, params);

        return user != null && user.getEmail() != null && !user.getEmail().equals("");
    }

    /**
     * Checks if provided user name matches with password. If match is found
     * true will be returned, otherwise false.
     *
     * @param password Password
     * @return
     */
    @Override
    public boolean checkCurrentUserPassword(String password) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_USERNAME_AND_PASSWORD);
        params.put(User.PARAM_PASSWORD, this.getPasswordHash(password));
        params.put(User.PARAM_USERNAME, getUserName());
        User user = getRepository().getEntity(User.class, params);

        return user != null && user.getUserName() != null && !user.getUserName().equals("");
    }

    /**
     * Returns true if user is active, otherwise false.
     *
     * @param userName User name
     * @return
     */
    @Override
    public boolean isUserActive(String userName) {
        User user = getUserInfo(userName);
        if (user != null) {
            return user.isActive();
        }
        return false;
    }

    /**
     * Returns true if user is active, otherwise false.
     *
     * @param email User email address
     * @return
     */
    @Override
    public boolean isUserActiveByEmail(String email) {
        User user = getUserByEmail(email);
        if (user != null) {
            return user.isActive();
        }
        return false;
    }

    /**
     * Returns the details for the currently authenticated user.
     *
     * <p>
     * No role is required to execute this method.</p>
     */
    @PermitAll
    @Override
    public User getCurrentUser() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_USERNAME);
        params.put(User.PARAM_USERNAME, this.getUserName());
        return getRepository().getEntity(User.class, params);
    }

    /**
     * Can be used to create a new user or save any updates to the details of an
     * existing user. Cannot be used to change the users password. This can only
     * be done using the
     * {@linkplain #changePassword(java.lang.String, java.lang.String) changePassword}
     * method.
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role. </p>
     *
     * @param user The details of the user to save
     * @return The user details after the save is completed
     */
    @RolesAllowed({RolesConstants.ADMIN_MANAGE_SECURITY, RolesConstants.ADMIN_CHANGE_PASSWORD})
    @Override
    public User saveUser(User user) {
        User oldUser = getUserInfo(user.getUserName());
        if (oldUser != null) {
            if (!oldUser.isActive() && user.isActive()) {
                // Send email
                if (systemEJB.isEmailServiceEnabled() && !StringUtility.isEmpty(user.getEmail())) {
                    String msgBody = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_ACTIVATION_BODY, "");
                    String msgSubject = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_ACTIVATION_SUBJECT, "");

                    msgBody = msgBody.replace(EmailVariables.FULL_USER_NAME, user.getFirstName());
                    msgBody = msgBody.replace(EmailVariables.USER_NAME, user.getUserName());

                    systemEJB.sendEmail(user.getFullName(), user.getEmail(), msgBody, msgSubject);
                }
            }
        }
        return getRepository().saveEntity(user);
    }

    /**
     * Saves current user
     *
     * @param user User object to be saved
     * @return
     */
    @Override
    public User saveCurrentUser(User user) {
        // Reset user name to be sure it's current user
        user.setUserName(getUserName());
        return getRepository().saveEntity(user);
    }

    /**
     * Creates Community Recorder user. CommunityRecorders group will be created
     * if it doesn't exist and appropriate roles assigned.
     *
     * @param user User object to be created as Community Recorder
     * @return
     */
    @Override
    public User createCommunityUser(User user) {
        // Check community group exists
        Group group = getRepository().getEntity(Group.class, Group.COMMUNITY_GROUP_ID);
        if (group == null) {
            // Create group and assign roles
            group = new Group();
            group.setId(Group.COMMUNITY_GROUP_ID);
            group.setName(Group.COMMUNITY_GROUP_NAME);
            group.setDescription(Group.COMMUNITY_GROUP_DESCRIPTION);
            getRepository().saveEntity(group);
        }

        // Generate activation code
        //String code = UUID.randomUUID().toString().substring(0, 8);
        //int timeOut = Integer.valueOf(systemEJB.getSetting(ConfigConstants.ACCOUNT_ACTIVATION_TIMEOUT, "70"));
        //user.setActivationCode(code);
        user.setActive(false);
        //user.setActivationExpiration(DateUtility.addTime(Calendar.getInstance().getTime(), timeOut, Calendar.HOUR));

        // Create user
        UserGroup ug = new UserGroup(user.getId(), group.getId());
        user.setUserGroups(new ArrayList<UserGroup>());
        user.getUserGroups().add(ug);
        String passwd = user.getPassword();
        user = getRepository().saveEntity(user);

        // Set password
        changeUserPassword(user.getUserName(), passwd);

        // Send email
        if (systemEJB.isEmailServiceEnabled() && !StringUtility.isEmpty(user.getEmail())) {
            String adminAddress = systemEJB.getSetting(ConfigConstants.EMAIL_ADMIN_ADDRESS, "");
            String adminName = systemEJB.getSetting(ConfigConstants.EMAIL_ADMIN_NAME, "");
            String msgBody = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_REG_BODY, "");
            String msgSubject = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_REG_SUBJECT, "");
            String activationPage = StringUtility.empty(LocalInfo.getBaseUrl());
            activationPage += "/user/regactivation.xhtml";
            //String activationUrl = activationPage + "?user=" + user.getUserName() + "&code=" + code;

            msgBody = msgBody.replace(EmailVariables.FULL_USER_NAME, user.getFirstName());
            msgBody = msgBody.replace(EmailVariables.USER_NAME, user.getUserName());
            //msgBody = msgBody.replace(EmailVariables.ACTIVATION_LINK, activationUrl);
            msgBody = msgBody.replace(EmailVariables.ACTIVATION_PAGE, activationPage);
            //msgBody = msgBody.replace(EmailVariables.ACTIVATION_CODE, code);

            systemEJB.sendEmail(user.getFullName(), user.getEmail(), msgBody, msgSubject);

            if (!adminAddress.equals("")) {
                // Send notification to admin
                String msgAdminBody = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_USER_REG_BODY, "");
                String msgAdminSubject = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_USER_REG_SUBJECT, "");
                msgAdminBody = msgAdminBody.replace(EmailVariables.USER_NAME, user.getUserName());

                systemEJB.sendEmail(adminName, adminAddress, msgAdminBody, msgAdminSubject);
            }
        }
        return user;
    }

    /**
     * Returns the list of all security roles in SOLA.
     *
     * <p>
     * No role is required to execute this method.</p>
     */
    @PermitAll
    @Override
    public List<Role> getRoles() {
        return getRepository().getEntityList(Role.class);
    }

    /**
     * Returns the role for the specified role code
     * <p>
     * No role is required to execute this method.</p>
     *
     * @param roleCode The role code to retrieve
     */
    @PermitAll
    @Override
    public Role getRole(String roleCode) {
        return getRepository().getEntity(Role.class, roleCode);
    }

    /**
     * Returns the list of all user groups supported by SOLA.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public List<Group> getGroups() {
        return getRepository().getEntityList(Group.class);
    }

    /**
     * Can be used to create a new user group or save any updates to the details
     * of an existing user group.
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role. </p>
     *
     * @param userGroup The details of the user group to save
     * @return The user group after the save is completed
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public Group saveGroup(Group userGroup) {
        return getRepository().saveEntity(userGroup);
    }

    /**
     * Returns the details for the specified group.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param groupId The identifier of the group to retrieve from the SOLA
     * database
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public Group getGroup(String groupId) {
        return getRepository().getEntity(Group.class, groupId);
    }

    /**
     * Can be used to create a new security role or save any updates to the
     * details of an existing security role.
     * <p>
     * Note that security roles are linked to the SOLA code base. Adding a new
     * role also requires updating code before SOLA will recognize the role</p>
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role. </p>
     *
     * @param role The details of the security role to save
     * @return The security role after the save is completed
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public Role saveRole(Role role) {
        return getRepository().saveEntity(role);
    }

    /**
     * Returns a summary list of all user groups supported by SOLA.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public List<GroupSummary> getGroupsSummary() {
        return getRepository().getEntityList(GroupSummary.class);
    }

    /**
     * Allows the users password to be changed
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_CHANGE_PASSWORD} role. </p>
     *
     * @param userName The username to change the password for
     * @param password The users new password
     * @return true if the change is successful.
     */
    @RolesAllowed({RolesConstants.ADMIN_MANAGE_SECURITY, RolesConstants.ADMIN_CHANGE_PASSWORD})
    @Override
    public boolean changePassword(String userName, String password) {
        return changeUserPassword(userName, password);
    }

    /**
     * Allows to change user's password by restore password code
     *
     * @param restoreCode Password restore code
     * @param password New user password
     * @return true if the change is successful.
     */
    @Override
    public boolean changePasswordByRestoreCode(String restoreCode, String password) {
        User user = getUserByActivationCode(restoreCode);
        if (user == null) {
            return false;
        }
        user.setActivationCode(null);
        getRepository().saveEntity(user);
        return changeUserPassword(user.getUserName(), password);
    }

    /**
     * Allows to change current user's password
     *
     * @param password
     * @return
     */
    @Override
    public boolean changeCurrentUserPassword(String password) {
        return changeUserPassword(getUserName(), password);
    }

    private boolean changeUserPassword(String userName, String password) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, User.QUERY_SET_PASSWORD);
        params.put(User.PARAM_PASSWORD, getPasswordHash(password));
        params.put(User.PARAM_USERNAME, userName);
        params.put(User.PARAM_CHANGE_USER, this.getUserName());

        ArrayList<HashMap> list = getRepository().executeFunction(params);

        if (list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
            return ((Integer) ((Entry) list.get(0).entrySet().iterator().next()).getValue()) > 0;
        } else {
            return false;
        }
    }

    /**
     * Returns SHA-256 hash for the password.
     *
     * @param password Password string to hash.
     */
    private String getPasswordHash(String password) {
        String hashString = null;

        if (password != null && password.length() > 0) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(password.getBytes("UTF-8"));
                byte[] hash = md.digest();

                // Ticket #410 - Fix password encyption. Ensure 0 is prepended
                // if the hex length is == 1 
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < hash.length; i++) {
                    String hex = Integer.toHexString(0xff & hash[i]);
                    if (hex.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(hex);
                }

                hashString = sb.toString();

            } catch (Exception e) {
                e.printStackTrace(System.err);
                return null;
            }
        }

        return hashString;
    }

    /**
     * Returns all roles associated to the specified username.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.
     * </p>
     *
     * @param userName The username to use for retrieval of the roles.
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    @Override
    public List<Role> getUserRoles(String userName) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, Role.QUERY_GET_ROLES_BY_USER_NAME);
        params.put(User.PARAM_USERNAME, userName);
        isInRole(userName);
        return getRepository().getEntityList(Role.class, params);
    }

    /**
     * Returns the list of all security roles assigned to the current user.
     *
     * <p>
     * No role is required to execute this method.</p>
     */
    @PermitAll
    @Override
    public List<Role> getCurrentUserRoles() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, Role.QUERY_GET_ROLES_BY_USER_NAME);
        params.put(User.PARAM_USERNAME, this.getUserName());
        return getRepository().getEntityList(Role.class, params);
    }

    /**
     * Checks if the current user has been assigned one or more of the null null
     * null null null null null null null null null null null null null null
     * null null null null null null null     {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY},
     * {@linkplain RolesConstants#ADMIN_MANAGE_REFDATA} or
     * {@linkplain RolesConstants#ADMIN_MANAGE_SETTINGS} security roles.
     * <p>
     * No role is required to execute this method.</p>
     *
     * @return true if the user is assigned one of the Admin security roles
     */
    @PermitAll
    @Override
    public boolean isUserAdmin() {
        return isInRole(RolesConstants.ADMIN_MANAGE_SECURITY, RolesConstants.ADMIN_MANAGE_REFDATA,
                RolesConstants.ADMIN_MANAGE_SETTINGS);
    }

    /**
     * Returns the list of languages supported by SOLA for localization in
     * priority order.
     *
     * <p>
     * No role is required to execute this method.</p>
     *
     * @param lang The language code to use to localize the display value for
     * each language.
     */
    @PermitAll
    @Override
    public List<Language> getLanguages(String lang) {
        Map params = new HashMap<String, Object>();
        if (lang != null) {
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
        }
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, "item_order");
        return getRepository().getEntityList(Language.class, params);
    }

    /**
     * Not used.
     *
     * @return The file name as it is saved in the server and ready for
     * download.
     *
     * @throws IOException
     */
    @RolesAllowed(RolesConstants.CONSOLIDATION_EXTRACT)
    @Override
    public String consolidationExtract(
            boolean generateConsolidationSchema, boolean everything, boolean dumpToFile) {
        String processName = String.format("extract_%s", DateUtility.formatDate(DateUtility.now(), "yyyy_MM_dd_HH_mm"));
        String[] commands = new String[8];

        commands[0] = systemEJB.getSetting("command-extract", "");
        commands[1] = getCurrentUser().getId();
        commands[2] = everything ? "Y" : "N";
        commands[3] = generateConsolidationSchema ? "Y" : "N";
        commands[4] = dumpToFile ? "Y" : "N";
        commands[5] = processName;
        commands[6] = systemEJB.getSetting("path-to-backup", "");
        commands[7] = systemEJB.getSetting("path-to-process-log", "");
        try {
            Runtime.getRuntime().exec(commands);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return processName;
    }

    /**
     * Not used.
     *
     * @param processName
     * @param languageCode
     * @param fileInServer
     * @param password
     * @return
     */
    @RolesAllowed(RolesConstants.CONSOLIDATION_CONSOLIDATE)
    @Override
    public String consolidationConsolidate(String extractedFile, boolean mergeConsolidationSchema) {
        String processName = String.format("consolidate_%s", DateUtility.formatDate(DateUtility.now(), "yyyy_MM_dd_HH_mm"));
        String[] commands = new String[8];

        commands[0] = systemEJB.getSetting("command-consolidate", "");
        commands[1] = getCurrentUser().getId();
        commands[2] = mergeConsolidationSchema ? "Y" : "N";
        commands[3] = extractedFile.isEmpty() ? "N/A" : extractedFile;
        commands[4] = processName;
        commands[5] = systemEJB.getSetting("path-to-backup", "");
        commands[6] = systemEJB.getSetting("path-to-process-log", "");
        try {
            Runtime.getRuntime().exec(commands);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return processName;
    }

    /**
     * Restores user password by generating activation code and sending a link
     * to the user for changing the password.
     *
     * @param email User's email
     */
    @Override
    public void restoreUserPassword(String email) {
        User user = getUserByEmail(email);
        if (user == null || StringUtility.isEmpty(user.getEmail()) || !user.isActive()) {
            return;
        }

        String code = UUID.randomUUID().toString();

        user.setActivationCode(code);
        user.setEntityAction(EntityAction.UPDATE);
        getRepository().saveEntity(user);

        // Send email
        if (systemEJB.isEmailServiceEnabled() && !StringUtility.isEmpty(user.getEmail())) {
            String msgBody = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_PASSWD_RESTORE_BODY, "");
            String msgSubject = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_PASSWD_RESTORE_SUBJECT, "");
            String restoreUrl = StringUtility.empty(LocalInfo.getBaseUrl()) + "/user/pwdrestore.xhtml?code=" + code;

            msgBody = msgBody.replace(EmailVariables.FULL_USER_NAME, user.getFirstName());
            msgBody = msgBody.replace(EmailVariables.PASSWORD_RESTORE_LINK, restoreUrl);

            systemEJB.sendEmail(user.getFullName(), user.getEmail(), msgBody, msgSubject);
        }
    }

    /**
     * Returns user by activation code
     *
     * @param activationCode Activation code
     * @return
     */
    @Override
    public User getUserByActivationCode(String activationCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, User.QUERY_WHERE_ACTIVATION_CODE);
        params.put(User.PARAM_ACTIVATION_CODE, activationCode);
        return getRepository().getEntity(User.class, params);
    }

    /**
     * It initializes a new process progress.
     *
     * @param processName Process name
     * @param maximumValue The maximum value the progress can get
     */
    @Override
    public void startProcessProgress(String processName, int maximumValue) {
        String sqlStatement = "select system.process_progress_start(#{process_name}, #{maximum_value}) as vl";
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
        params.put("process_name", processName);
        params.put("maximum_value", maximumValue);
        getRepository().getScalar(Void.class, params);
    }

    /**
     * Returns the progress of a certain process.
     *
     * @param processName process name
     * @param inPercentage True - the value in percentage, otherwise the
     * absolute value
     * @return
     */
    @Override
    public int getProcessProgress(String processName, boolean inPercentage) {
        String sqlStatement = "select system.process_progress_get(#{process_name}) as vl";
        if (inPercentage) {
            sqlStatement = "select system.process_progress_get_in_percentage(#{process_name}) as vl";
        }
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
        params.put("process_name", processName);
        return getRepository().getScalar(Integer.class, params);
    }

    /**
     * Sets the progress in absolute value of the progress of a process.
     *
     * @param processName process name
     * @param progressValue progress value
     */
    @Override
    public void setProcessProgress(String processName, int progressValue) {
        String sqlStatement = "select system.process_progress_set(#{process_name}, #{progress_value}) as vl";
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
        params.put("process_name", processName);
        params.put("progress_value", progressValue);
        getRepository().getScalar(Void.class, params);
    }

    /**
     * Not used.
     *
     * @param processName
     * @return
     */
    @Override
    public String getProcessLog(String processName) {
        processName = processName + ".log";
        String logFilename = FileUtility.sanitizeFileName(processName, true);
        String pathToProcessLog = systemEJB.getSetting("path-to-process-log", "");
        String fullPathToLog = String.format("%s/%s", pathToProcessLog, logFilename);
        try {
            return FileUtils.readFileToString(new File(fullPathToLog));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Updates the security classifications for a list of entities and
     * identified by the entityTable and entity ids
     *
     * @param entityIds The ids of the entities to update
     * @param entityTable Enumeration indicating the entity table to update
     * @param classificationCode The new classification code to assign to the
     * entities
     * @param redactCode The new redactCode to assign to the entities
     */
    @RolesAllowed(RolesConstants.CLASSIFICATION_CHANGE_CLASS)
    @Override
    public void saveSecurityClassifications(List<String> entityIds, EntityTable entityTable,
            String classificationCode, String redactCode) {

        Map params = new HashMap<String, Object>();
        String updateSql = " UPDATE " + entityTable.getTable()
                + " SET classification_code = #{classCode}, "
                + "     redact_code = #{redactCode}, "
                + "     change_user = #{user} "
                + " WHERE id IN ("
                + CommonSqlProvider.prepareListParams(entityIds, params) + ") ";

        params.put(CommonSqlProvider.PARAM_QUERY, updateSql);
        params.put("classCode", classificationCode);
        params.put("redactCode", redactCode);
        params.put("user", getCurrentUser().getUserName());
        getRepository().bulkUpdate(params);
    }

    @Override
    public boolean resetCache() {
        String ejbLookupName;
        Object cahceEjb;
        InitialContext ic = null;
        
        try {
            // Try to find cache EJB
            ejbLookupName = "java:app/CacheEJBLocal";
            ic = new InitialContext();
            cahceEjb = ic.lookup(ejbLookupName);
            cahceEjb.getClass().getMethod("clearAll").invoke(cahceEjb);
        } catch (Exception ex) {
            try {
                // Try to clear CS cache
                ejbLookupName = "java:app/CacheCSEJBLocal";
                cahceEjb = ic.lookup(ejbLookupName);
                cahceEjb.getClass().getMethod("clearAll").invoke(cahceEjb);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
