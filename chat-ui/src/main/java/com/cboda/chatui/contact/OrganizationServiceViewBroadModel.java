/*
 * Copyright (c) 2023 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.net.SimpleCallback;
import com.cboda.chatui.organization.OrganizationServiceBroadProvider;
import com.cboda.chatui.organization.model.Employee;
import com.cboda.chatui.organization.model.EmployeeBroadEx;
import com.cboda.chatui.organization.model.Organization;
import com.cboda.chatui.organization.model.OrganizationBroadEx;
import com.cboda.chatui.organization.model.OrganizationBroadRelationship;
import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.OnConnectionStatusChangeListener;

public class OrganizationServiceViewBroadModel extends ViewModel implements OnConnectionStatusChangeListener {
    private static final String TAG = OrganizationServiceViewBroadModel.class.getSimpleName();
    private MutableLiveData<List<Organization>> rootOrganizationLiveData;
    private MutableLiveData<List<Organization>> myOrganizationLiveData;
    private MutableLiveData<List<Employee>> organizationEmployeeLiveData;
    private final OrganizationServiceBroadProvider organizationServiceProvider;

    public OrganizationServiceViewBroadModel() {
        ChatManager.Instance().addConnectionChangeListener(this);

        organizationServiceProvider = WfcUIBroadKit.getWfcUIKit().getOrganizationServiceProvider();

        if (organizationServiceProvider != null
            && !organizationServiceProvider.isServiceAvailable()
            && ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusConnected) {
            login();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ChatManager.Instance().removeConnectionChangeListener(this);
    }

    @Override
    public void onConnectionStatusChange(int status) {
        if (status == ConnectionStatus.ConnectionStatusConnected) {
            login();
        }
    }

    public MutableLiveData<List<Organization>> rootOrganizationLiveData() {
        if (rootOrganizationLiveData == null) {
            rootOrganizationLiveData = new MutableLiveData<>();
        }
        if (organizationServiceProvider.isServiceAvailable()) {
            loadRootOrganizations();
        }
        return rootOrganizationLiveData;
    }

    public MutableLiveData<List<Organization>> myOrganizationLiveData() {
        if (myOrganizationLiveData == null) {
            myOrganizationLiveData = new MutableLiveData<>();
        }
        if (organizationServiceProvider.isServiceAvailable()) {
            loadMyOrganizations();
        }

        return myOrganizationLiveData;
    }

    public MutableLiveData<List<String>> getOrganizationEmployees(int orgId) {
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();
        organizationServiceProvider.getOrgEmployees(orgId, new SimpleCallback<List<String>>() {
            @Override
            public void onUiSuccess(List<String> strings) {
                liveData.postValue(strings);
            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
        return liveData;
    }

    public MutableLiveData<List<Organization>> getOrganizations(List<Integer> ids) {
        MutableLiveData<List<Organization>> liveData = new MutableLiveData<>();
        organizationServiceProvider.getOrganizations(ids, new SimpleCallback<List<Organization>>() {
            @Override
            public void onUiSuccess(List<Organization> organizations) {
                liveData.postValue(organizations);
            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
        return liveData;
    }

    public MutableLiveData<OrganizationBroadEx> getOrganizationEx(int organizationId) {
        MutableLiveData<OrganizationBroadEx> liveData = new MutableLiveData<>();
        OrganizationServiceBroadProvider organizationServiceProvider = WfcUIBroadKit.getWfcUIKit().getOrganizationServiceProvider();
        organizationServiceProvider.getOrganizationEx(organizationId, new SimpleCallback<OrganizationBroadEx>() {
            @Override
            public void onUiSuccess(OrganizationBroadEx organizationEx) {
                liveData.setValue(organizationEx);
            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
        return liveData;
    }

    public MutableLiveData<List<Employee>> getOrganizationEmployees(List<Integer> organizationIds, boolean includeSubOrganization) {
        MutableLiveData<List<Employee>> liveData = new MutableLiveData<>();

        List<Employee> employees = new ArrayList<>();
        ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                CountDownLatch latch;
                if (includeSubOrganization) {
                    latch = new CountDownLatch(1);
                    organizationServiceProvider.getOrganizationEmployees(organizationIds, new SimpleCallback<List<Employee>>() {
                        @Override
                        public void onUiSuccess(List<Employee> es) {
                            employees.addAll(es);
                            latch.countDown();
                        }

                        @Override
                        public void onUiFailure(int code, String msg) {
                            latch.countDown();
                        }
                    });


                } else {
                    latch = new CountDownLatch(organizationIds.size());
                    for (Integer orgId : organizationIds) {
                        organizationServiceProvider.getOrganizationEx(orgId, new SimpleCallback<OrganizationBroadEx>() {
                            @Override
                            public void onUiSuccess(OrganizationBroadEx organizationEx) {
                                if (organizationEx != null && organizationEx.employees != null) {
                                    employees.addAll(organizationEx.employees);
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onUiFailure(int code, String msg) {
                                latch.countDown();
                            }
                        });
                    }
                }
                try {
                    latch.await();
                    liveData.postValue(employees);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return liveData;
    }

    public MutableLiveData<List<Organization>> getOrganizationPath(int organizationId) {
        MutableLiveData<List<Organization>> liveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                List<Organization> pathList = new ArrayList<>();
                getOrganizationPathSync(organizationId, pathList);
                Log.d("PickOrgMemberFragment", "pathList " + organizationId + " " + pathList.size());
                liveData.postValue(pathList);
            }
        });

        return liveData;
    }

    public MutableLiveData<EmployeeBroadEx> getEmployeeEx(String userId) {
        MutableLiveData<EmployeeBroadEx> liveData = new MutableLiveData<>();
        organizationServiceProvider.getEmployeeEx(userId, new SimpleCallback<EmployeeBroadEx>() {
            @Override
            public void onUiSuccess(EmployeeBroadEx employeeEx) {
                liveData.postValue(employeeEx);

            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
        return liveData;
    }

    private void getOrganizationPathSync(int orgId, List<Organization> outPathList) {
        Organization org = getOrganizationSync(orgId);
        if (org != null) {
            outPathList.add(0, org);
            if (org.parentId != 0) {
                getOrganizationPathSync(org.parentId, outPathList);
            }
        }
    }

    private Organization getOrganizationSync(int orgId) {
        CountDownLatch latch = new CountDownLatch(1);
        final Organization[] orgArr = new Organization[1];
        organizationServiceProvider.getOrganizations(Collections.singletonList(orgId), new SimpleCallback<List<Organization>>() {
            @Override
            public void onUiSuccess(List<Organization> organizations) {
                if (!organizations.isEmpty()) {
                    orgArr[0] = organizations.get(0);
                }
                latch.countDown();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                latch.countDown();
                Log.e(TAG, "getOrganizations error " + code + " " + msg);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return orgArr[0];
    }

    private void login() {
        if (organizationServiceProvider != null && !organizationServiceProvider.isServiceAvailable()) {
            organizationServiceProvider.login(new GeneralCallback() {
                @Override
                public void onSuccess() {
                    if (rootOrganizationLiveData != null) {
                        loadRootOrganizations();
                        loadMyOrganizations();
                    }
                }

                @Override
                public void onFail(int errorCode) {
                    Log.e(TAG, "login failed " + errorCode);

                }
            });
        }
    }


    private void loadRootOrganizations() {
        ChatManager.Instance().getWorkHandler().post(() -> {
            organizationServiceProvider.getRootOrganization(new SimpleCallback<List<Organization>>() {
                @Override
                public void onUiSuccess(List<Organization> organizations) {
                    rootOrganizationLiveData.setValue(organizations);
                }

                @Override
                public void onUiFailure(int code, String msg) {

                }
            });
        });
    }

    private void loadMyOrganizations() {
        organizationServiceProvider.getRelationship(ChatManager.Instance().getUserId(), new SimpleCallback<List<OrganizationBroadRelationship>>() {
            @Override
            public void onUiSuccess(List<OrganizationBroadRelationship> organizationRelationships) {
                List<Integer> orgIds = new ArrayList<>();
                if (organizationRelationships != null && !organizationRelationships.isEmpty()) {
                    for (OrganizationBroadRelationship r : organizationRelationships) {
                        if (r.bottom) {
                            orgIds.add(r.organizationId);
                        }
                    }
                    if (!orgIds.isEmpty()) {
                        organizationServiceProvider.getOrganizations(orgIds, new SimpleCallback<List<Organization>>() {
                            @Override
                            public void onUiSuccess(List<Organization> organizations) {
                                myOrganizationLiveData.setValue(organizations);
                            }

                            @Override
                            public void onUiFailure(int code, String msg) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
    }
}
