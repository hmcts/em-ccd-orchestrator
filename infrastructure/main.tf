provider "azurerm" {
  features {}
}

locals {
  app_full_name       = "${var.product}-${var.component}"
  ase_name            = "core-compute-${var.env}"
  local_env           = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env}"
  shared_vault_name   = "${local.app_full_name}-${local.local_env}"
  s2s_key             = data.azurerm_key_vault_secret.s2s_key.value
  resource_group_name = "${local.app_full_name}-${var.env}"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

resource "azurerm_resource_group" "rg" {
  name     = local.resource_group_name
  location = var.location

  tags = var.common_tags
}

data "azurerm_user_assigned_identity" "ia_aat_identity" {
  count               = var.env == "preview" ? 1 : 0
  name                = "ia-aat-mi"
  resource_group_name = "managed-identities-aat-rg"
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_key" {
  name         = "microservicekey-em-ccd-orchestrator"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

module "local_key_vault" {
  source                               = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product                              = local.app_full_name
  env                                  = var.env
  tenant_id                            = var.tenant_id
  object_id                            = var.jenkins_AAD_objectId
  resource_group_name                  = azurerm_resource_group.rg.name
  product_group_object_id              = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags                          = var.common_tags
  managed_identity_object_ids          = ["${data.azurerm_user_assigned_identity.rpa-shared-identity.principal_id}"]
  additional_managed_identities_access = var.env == "preview" ? [data.azurerm_user_assigned_identity.ia_aat_identity.principal_id] : []
}

data "azurerm_user_assigned_identity" "rpa-shared-identity" {
  name                = "rpa-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

resource "azurerm_key_vault_secret" "local_s2s_key" {
  name         = "microservicekey-em-ccd-orchestrator"
  value        = data.azurerm_key_vault_secret.s2s_key.value
  key_vault_id = module.local_key_vault.key_vault_id
}

data "azurerm_key_vault" "shared_key_vault" {
  name                = "rpa-${var.env}"
  resource_group_name = "rpa-${var.env}"
}

# Load AppInsights key from rpa vault
data "azurerm_key_vault_secret" "shared_app_insights_key" {
  name         = "EmAppInsightsInstrumentationKey"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "appinsights_key" {
  name         = "AppInsightsInstrumentationKey"
  value        = data.azurerm_key_vault_secret.shared_app_insights_key.value
  key_vault_id = module.local_key_vault.key_vault_id
}

data "azurerm_key_vault_secret" "shared_app_insights_connection_string" {
  name         = "em-app-insights-connection-string"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_key_vault_secret.shared_app_insights_connection_string.value
  key_vault_id = module.local_key_vault.key_vault_id
}
