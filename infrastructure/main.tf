provider "azurerm" {
  version = "1.23.0"
}
locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "core-compute-${var.env}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  shared_vault_name = "${var.shared_product_name}-${local.local_env}"
  s2s_key = "${data.azurerm_key_vault_secret.s2s_key.value}"
  resource_group_name = "${var.shared_product_name}-${var.env}"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "azurerm_key_vault" "s2s_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_key" {
  name      = "microservicekey-em-ccd-orchestrator"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

data "azurerm_key_vault" "shared_key_vault" {
  name = "${local.shared_vault_name}"
  resource_group_name = "${local.shared_vault_name}"
}

module "local_key_vault" {
  source = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product = "${local.app_full_name}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.app.resource_group_name}"
  product_group_object_id = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags = "${var.common_tags}"
  managed_identity_object_id = "${var.managed_identity_object_id}"
}

# Copy s2s key from shared to local vault
data "azurerm_key_vault" "local_key_vault" {
  name = "${module.local_key_vault.key_vault_name}"
  resource_group_name = "${module.local_key_vault.key_vault_name}"
}

resource "azurerm_key_vault_secret" "local_s2s_key" {
  name         = "microservicekey-em-ccd-orchestrator"
  value        = "${data.azurerm_key_vault_secret.s2s_key.value}"
  key_vault_id = "${data.azurerm_key_vault.local_key_vault.id}"
}
