variable "product" {
  type = "string"
}

variable "shared_product_name" {
  default = "rpa"
}

variable "component" {
  type = "string"
}

variable "team_name" {
  default = "evidence"
}

variable "app_language" {
  default = "java"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "common_tags" {
  type = "map"
}
////////////////////////////////////////////////
//Addtional Vars ///////////////////////////////
////////////////////////////////////////////////
variable "capacity" {
  default = "1"
}

variable "java_opts" {
  default = ""
}
////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net:80"
}

variable "s2s_url" {
  default = "rpe-service-auth-provider"
}

variable "dm_store_app_url" {
  default = "dm-store"
}

variable "em_stitching_api_url" {
  default = "em-stitching"
}

variable "postgresql_user" {
  default = "annotation"
}

variable "database_name" {
  default = "emstitch"
}
////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////
variable "root_appender" {
  default = "JSON_CONSOLE"
}

variable "json_console_pretty_print" {
  default = "false"
}

variable "log_output" {
  default = "single"
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_dm" {
  default = "INFO"
}

variable "show_sql" {
  default = "true"
}

variable "endpoints_health_sensitive" {
  default = "true"
}

variable "endpoints_info_sensitive" {
  default = "true"
}
////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
variable "enable_idam_healthcheck" {
    default = "false"
}

variable "enable_s2s_healthcheck" {
    default = "false"
}

////////////////////////////////////////////////
// Whitelists
////////////////////////////////////////////////
variable "s2s_names_whitelist" {
  default = "em_api,em_gw,ccd_gw,ccd_data,sscs,divorce_document_upload,divorce_document_generator,probate_backend,jui_webapp,pui_webapp"
}

variable "case_worker_roles" {
  default = "caseworker-probate,caseworker-cmc,caseworker-sscs,caseworker-divorce"
}
////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
