/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.api;


import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.services.WorkspacesApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class WorkspacesApiTest {

    @Test
    public void createWorkspaceTest() throws ApiException {
        WorkspaceDTO workspace = new WorkspaceDTO();
        String workspaceId = TestUtils.randomString();
        workspace.setId(workspaceId);
        workspace.setDescription("Generated by tests");
        workspace.setFolderLocked(false);
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        WorkspaceDTO createdWorkspace = workspacesApi.createWorkspace(workspace, TestConfig.LOGIN);
        workspace.setEnabled(createdWorkspace.getEnabled());
        Assert.assertEquals(workspace, createdWorkspace);
        workspacesApi.deleteWorkspace(workspaceId);
    }

    @Test
    public void getWorkspaceList() throws ApiException {
        WorkspaceDTO workspace = new WorkspaceDTO();
        String workspaceId = TestUtils.randomString();
        workspace.setId(workspaceId);
        workspace.setDescription("Generated by tests");
        workspace.setFolderLocked(false);
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        WorkspaceDTO createdWorkspace = workspacesApi.createWorkspace(workspace, TestConfig.LOGIN);
        WorkspaceListDTO workspacesForConnectedUser = workspacesApi.getWorkspacesForConnectedUser();
        Assert.assertTrue(workspacesForConnectedUser.getAllWorkspaces().contains(createdWorkspace));
        workspacesApi.deleteWorkspace(workspaceId);
    }

    @Test
    public void updateWorkspace() throws ApiException {

        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        WorkspaceDTO workspace = TestUtils.createWorkspace();

        String newDescription = "Updated by tests";
        workspace.setDescription(newDescription);
        WorkspaceDTO updatedWorkspace = workspacesApi.updateWorkspace(workspace.getId(), workspace);

        Assert.assertEquals(updatedWorkspace.getDescription(), newDescription);
        Assert.assertEquals(updatedWorkspace, workspace);

        workspacesApi.deleteWorkspace(workspace.getId());
    }

    @Test
    public void addUserInWorkspace() throws ApiException {
        AccountDTO newAccount = TestUtils.createAccount();
        UserDTO userToAdd = new UserDTO();
        userToAdd.setLogin(newAccount.getLogin());
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        workspacesApi.addUser(TestConfig.WORKSPACE, userToAdd, null);
        List<UserDTO> usersInWorkspace = workspacesApi.getUsersInWorkspace(TestConfig.WORKSPACE);
        Assert.assertEquals(usersInWorkspace.stream().filter(userDTO -> userDTO.getLogin().equals(userToAdd.getLogin())).count(), 1);
    }

    @Test
    public void addUserInGroup() throws ApiException {
        AccountDTO newAccount = TestUtils.createAccount();
        UserDTO userToAdd = new UserDTO();
        userToAdd.setLogin(newAccount.getLogin());
        UserGroupDTO group = createGroup();
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        workspacesApi.addUser(TestConfig.WORKSPACE, userToAdd, group.getId());
        List<UserDTO> usersInGroup = workspacesApi.getUsersInGroup(TestConfig.WORKSPACE, group.getId());
        Assert.assertEquals(usersInGroup.stream().filter(userDTO -> userDTO.getLogin().equals(userToAdd.getLogin())).count(), 1);
    }

    @Test
    public void forbiddenDeleteWorkspaceTest() throws ApiException, InterruptedException {
        WorkspaceDTO workspace = new WorkspaceDTO();
        String workspaceId = TestUtils.randomString();
        workspace.setId(workspaceId);
        workspace.setDescription("Generated by tests");
        workspace.setFolderLocked(false);
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
        WorkspaceDTO createdWorkspace = workspacesApi.createWorkspace(workspace, TestConfig.LOGIN);

        AccountDTO newAccount = TestUtils.createAccount();
        UserDTO userToAdd = new UserDTO();
        userToAdd.setLogin(newAccount.getLogin());
        workspacesApi.addUser(workspaceId, userToAdd, null);

        workspacesApi.setNewAdmin(workspaceId, userToAdd);
        try {
            workspacesApi.deleteWorkspace(workspaceId);
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }

        WorkspaceListDTO workspacesForConnectedUser = workspacesApi.getWorkspacesForConnectedUser();
        Assert.assertTrue(workspacesForConnectedUser.getAllWorkspaces().contains(createdWorkspace));
        Assert.assertTrue(!workspacesForConnectedUser.getAdministratedWorkspaces().contains(createdWorkspace));

        ApiClient newAdminClient = DocdokuPLMClientFactory.createBasicClient(TestConfig.URL, userToAdd.getLogin(),
                TestConfig.PASSWORD, TestConfig.DEBUG);

        new WorkspacesApi(newAdminClient).deleteWorkspace(workspaceId);
        Thread.sleep(2000);

        workspacesForConnectedUser = workspacesApi.getWorkspacesForConnectedUser();
        Assert.assertTrue(!workspacesForConnectedUser.getAllWorkspaces().contains(createdWorkspace));
    }

    private UserGroupDTO createGroup() throws ApiException {
        String groupId = TestUtils.randomString();
        UserGroupDTO group = new UserGroupDTO();
        group.setWorkspaceId(TestConfig.WORKSPACE);
        group.setId(groupId);
        return new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT).createGroup(TestConfig.WORKSPACE, group);
    }

}
