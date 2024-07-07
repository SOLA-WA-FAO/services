package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.public_display_status code table.
 */
@Table(name = "public_display_status", schema = "application")
@DefaultSorter(sortString="display_value")
public class PublicDisplayStatus extends AbstractCodeEntity {
    public PublicDisplayStatus() {
        super();
    }
}