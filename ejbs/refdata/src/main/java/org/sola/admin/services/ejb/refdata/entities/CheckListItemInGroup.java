package org.sola.admin.services.ejb.refdata.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 * Entity representing the application.authority code table.
 */
@Table(name = "checklist_item_in_group", schema = "application")
public class CheckListItemInGroup extends AbstractEntity {
    
    @Id
    @Column(name = "checklist_group_code")
    String checkListGroupCode;

    @Id
    @Column(name = "checklist_item_code")
    String checkListItemCode;
    
    public CheckListItemInGroup() {
        super();
    }

    public String getCheckListGroupCode() {
        return checkListGroupCode;
    }

    public void setCheckListGroupCode(String checkListGroupCode) {
        this.checkListGroupCode = checkListGroupCode;
    }

    public String getCheckListItemCode() {
        return checkListItemCode;
    }

    public void setCheckListItemCode(String checkListItemCode) {
        this.checkListItemCode = checkListItemCode;
    }
}
