package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the administrative.notation_status_type code table.
 */
@Table(name = "notation_status_type", schema = "administrative")
@DefaultSorter(sortString="display_value")
public class NotationStatusType extends AbstractCodeEntity {
    
    public NotationStatusType() {
        super();
    }
}
