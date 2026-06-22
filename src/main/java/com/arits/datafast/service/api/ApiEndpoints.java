package com.arits.datafast.service.api;

public final class ApiEndpoints {


    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------
    public static final class Auth {

        public static final String LOGIN                  = "/api/login";
        public static final String LOGOUT                = "/api/logout";
        public static final String SIGN_UP               = "/api/sign-up";
        public static final String FORGOT_PASSWORD       = "/api/forgot-password-request";
        public static final String RESET_PASSWORD        = "/api/forgot-reset-password";
        public static final String USER_DETAILS          = "/api/user";

    }


    // -------------------------------------------------------------------------
    // Companies
    // -------------------------------------------------------------------------
    public static final class Companies {

        public static final String CREATE = "/api/companies/create";
        public static final String LIST   = "/api/companies/list";

        public static String getOne(int id)    { return "/api/companies/" + id; }
        public static String update(int id)    { return "/api/companies/" + id; }
        public static String delete(int id)    { return "/api/companies/" + id; }

    }


    // -------------------------------------------------------------------------
    // Automation — Modules (portals)
    // -------------------------------------------------------------------------
    public static final class Modules {

        public static final String CREATE = "/api/automations/modules/create";
        public static final String LIST   = "/api/automations/modules/list";

        public static String getOne(int id)  { return "/api/automations/modules/" + id; }
        public static String update(int id)  { return "/api/automations/modules/" + id + "/update"; }

    }


    // -------------------------------------------------------------------------
    // Automation — Sub-modules (automations inside a module)
    // -------------------------------------------------------------------------
    public static final class SubModules {

        public static final String CREATE = "/api/automations/sub-modules/create";
        public static final String LIST   = "/api/automations/sub-modules/list";

        public static String getOne(int id)  { return "/api/automations/sub-modules/" + id; }
        public static String update(int id)  { return "/api/automations/sub-modules/" + id + "/update"; }
        public static String delete(int id)  { return "/api/automations/sub-modules/" + id + "/delete"; }

    }


    // -------------------------------------------------------------------------
    // Automation — Action Types
    // -------------------------------------------------------------------------
    public static final class ActionTypes {

        public static final String CREATE = "/api/automations/action-types/create";
        public static final String LIST   = "/api/automations/action-types/list";

        public static String getOne(int id)  { return "/api/automations/action-types/" + id; }
        public static String update(int id)  { return "/api/automations/action-types/" + id + "/update"; }

    }



    // -------------------------------------------------------------------------
    // Automation — Form Field Mappings
    // -------------------------------------------------------------------------
    public static final class FormFieldMappings {

        public static final String CREATE         = "/api/automations/form-field-mappings/create";
        public static final String CREATE_GENERIC = "/api/automations/form-field-mappings/create/generic-field";
        public static final String UPSERT         = "/api/automations/form-field-mappings/upsert";

        public static String getOne(int id)          { return "/api/automations/form-field-mappings/" + id; }
        public static String update(int id)           { return "/api/automations/form-field-mappings/" + id + "/update"; }
        public static String delete(int id)           { return "/api/automations/form-field-mappings/" + id + "/delete"; }
        public static String updateGeneric(int id)    { return "/api/automations/form-field-mappings/" + id + "/update/generic-field"; }
        public static String deleteGeneric(int id)    { return "/api/automations/form-field-mappings/" + id + "/generic-field/delete"; }
        public static String listByAutomationAndCompany(int automationId, int companyId) { return "/api/automations/form-field-mappings/list/" + automationId + "/" + companyId;}
        public static String genericFieldList(int automationId) { return "/api/automations/form-field-mappings/generic-field-list/" + automationId;}

    }

    // -------------------------------------------------------------------------
    // Automation — Logs
    // -------------------------------------------------------------------------
    public static final class AutomationLogs {

        public static final String CREATE = "/api/automations/automation-logs/create";

        public static String getOne(int id)   { return "/api/automations/automation-logs/" + id; }
        public static String getFile(int id)  { return "/api/automations/automation-logs/" + id + "/file"; }
        public static String update(int id)   { return "/api/automations/automation-logs/" + id + "/update"; }
        public static String listAll(int automationId) { return "/api/automations/automation-logs/list-all/" + automationId;}
        public static String listByAutomationAndCompany(int automationId, int companyId) { return "/api/automations/automation-logs/list/" + automationId + "/" + companyId;}

    }
}