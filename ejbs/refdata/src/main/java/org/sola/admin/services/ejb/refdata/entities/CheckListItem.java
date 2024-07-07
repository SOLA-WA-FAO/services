package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.authority code table.
 */
@Table(name = "checklist_item", schema = "application")
@DefaultSorter(sortString="display_order")
public class CheckListItem extends AbstractCodeEntity {
    @Column(name = "display_order")
    int displayOrder;
    
    public CheckListItem() {
        super();
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}