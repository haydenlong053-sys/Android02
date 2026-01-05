/*
 * Copyright (c) 2023 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.organization;

import java.util.List;

import com.cboda.chatui.net.SimpleCallback;
import com.cboda.chatui.organization.model.Employee;
import com.cboda.chatui.organization.model.EmployeeBroadEx;
import com.cboda.chatui.organization.model.Organization;
import com.cboda.chatui.organization.model.OrganizationBroadEx;
import com.cboda.chatui.organization.model.OrganizationBroadRelationship;
import cn.wildfirechat.remote.GeneralCallback;

public interface OrganizationServiceBroadProvider {
    void login(GeneralCallback callback);

    boolean isServiceAvailable();

    void getRelationship(String employeeId, SimpleCallback<List<OrganizationBroadRelationship>> callback);

    void getRootOrganization(SimpleCallback<List<Organization>> callback);

    void getOrganizationEx(int orgId, SimpleCallback<OrganizationBroadEx> callback);

    void getOrganizations(List<Integer> orgIds, SimpleCallback<List<Organization>> callback);

    void getOrganizationEmployees(List<Integer> orgIds, SimpleCallback<List<Employee>> callback);

    void getOrgEmployees(List<Integer> orgIds, SimpleCallback<List<String>> callback);

    void getOrgEmployees(int orgId, SimpleCallback<List<String>> callback);

    void getEmployee(String employeeId, SimpleCallback<Employee> callback);

    void getEmployees(List<String> employeeIds, SimpleCallback<List<Employee>> callback);

    void getEmployeeEx(String employeeId, SimpleCallback<EmployeeBroadEx> callback);

    void searchEmployee(int orgId, String keyword, SimpleCallback<List<Employee>> callback);

}
