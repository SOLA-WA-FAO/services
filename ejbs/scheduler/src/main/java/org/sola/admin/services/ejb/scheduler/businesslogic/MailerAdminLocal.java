package org.sola.admin.services.ejb.scheduler.businesslogic;

import jakarta.ejb.Local;

@Local
public interface MailerAdminLocal {
    void init();
}
