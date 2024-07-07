package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.negotiate_type code table.
 */
@Table(name = "negotiate_type", schema = "application")
@DefaultSorter(sortString="display_value")
public class NegotiateType extends AbstractCodeEntity {
    public NegotiateType() {
        super();
    }
}