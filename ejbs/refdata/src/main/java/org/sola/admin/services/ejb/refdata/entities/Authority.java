package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.authority code table.
 */
@Table(name = "authority", schema = "application")
@DefaultSorter(sortString="display_value")
public class Authority extends AbstractCodeEntity {
    
    public Authority() {
        super();
    }
}
