package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the administrative.valuation_type code table.
 */
@Table(name = "valuation_type", schema = "administrative")
@DefaultSorter(sortString="display_value")
public class ValuationType extends AbstractCodeEntity {
    
    public ValuationType() {
        super();
    }
}
