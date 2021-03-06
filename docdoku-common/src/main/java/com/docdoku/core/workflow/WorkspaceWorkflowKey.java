/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.workflow;

import java.io.Serializable;

/**
 * Identity class of {@link WorkspaceWorkflow} objects.
 *
 * @author Florent Garin
 */
public class WorkspaceWorkflowKey implements Serializable, Comparable<WorkspaceWorkflowKey>, Cloneable {

    private String workspace;
    private String id;


    public WorkspaceWorkflowKey() {
    }

    public WorkspaceWorkflowKey(String pWorkspaceId, String pId) {
        workspace = pWorkspaceId;
        id = pId;
    }


    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String pWorkspaceId) {
        workspace = pWorkspaceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }


    @Override
    public String toString() {
        return workspace + "-" + id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkspaceWorkflowKey that = (WorkspaceWorkflowKey) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (!workspace.equals(that.workspace)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = workspace.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }


    public int compareTo(WorkspaceWorkflowKey pKey) {
        int wksComp = workspace.compareTo(pKey.workspace);
        if (wksComp != 0) {
            return wksComp;
        } else {
            return id.compareTo(pKey.id);
        }
    }

    @Override
    public WorkspaceWorkflowKey clone() {
        WorkspaceWorkflowKey clone;
        try {
            clone = (WorkspaceWorkflowKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}