package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.objection_status code table.
 */
@Table(name = "objection_status", schema = "application")
@DefaultSorter(sortString="display_value")
public class ObjectionStatus extends AbstractCodeEntity {
    public ObjectionStatus() {
        super();
    }
}