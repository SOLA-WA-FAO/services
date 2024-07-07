package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.public_display_type code table.
 */
@Table(name = "public_display_type", schema = "application")
@DefaultSorter(sortString="display_value")
public class PublicDisplayType extends AbstractCodeEntity {
    public PublicDisplayType() {
        super();
    }
}