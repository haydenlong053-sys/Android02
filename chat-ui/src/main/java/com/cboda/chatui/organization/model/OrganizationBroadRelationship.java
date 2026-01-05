/*
 * Copyright (c) 2023 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.organization.model;

public class OrganizationBroadRelationship {
    public String employeeId;
    public int organizationId;
    public int depth;
    // 相当于是否是子节点的意思
    public boolean bottom;
    public int parentOrganizationId;
}
