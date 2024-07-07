package org.sola.admin.services.ejb.refdata.entities;

import java.util.List;
import jakarta.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.authority code table.
 */
@Table(name = "checklist_group", schema = "application")
@DefaultSorter(sortString="display_value")
public class CheckListGroup extends AbstractCodeEntity {
    
    @ChildEntityList(parentIdField = "checkListGroupCode")
    List<CheckListItemInGroup> checkListItems;
    
    public CheckListGroup() {
        super();
    }

    public List<CheckListItemInGroup> getCheckListItems() {
        return checkListItems;
    }

    public void setCheckListItems(List<CheckListItemInGroup> checkListItems) {
        this.checkListItems = checkListItems;
    }
}