package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.negotiate_status code table.
 */
@Table(name = "negotiate_status", schema = "application")
@DefaultSorter(sortString="display_value")
public class NegotiateStatus extends AbstractCodeEntity {
    public NegotiateStatus() {
        super();
    }
}
