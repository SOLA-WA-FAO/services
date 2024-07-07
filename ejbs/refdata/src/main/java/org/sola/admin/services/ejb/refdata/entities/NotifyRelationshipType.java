package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.notify_relationship_type code table.
 */
@Table(name = "notify_relationship_type", schema = "application")
@DefaultSorter(sortString="display_value")
public class NotifyRelationshipType extends AbstractCodeEntity {
    public NotifyRelationshipType() {
        super();
    }
}