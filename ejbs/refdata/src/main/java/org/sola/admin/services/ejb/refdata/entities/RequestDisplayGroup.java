package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.request_display_group code table.
 */
@Table(name = "request_display_group", schema = "application")
@DefaultSorter(sortString="display_value")
public class RequestDisplayGroup extends AbstractCodeEntity {
    public RequestDisplayGroup() {
        super();
    }
}