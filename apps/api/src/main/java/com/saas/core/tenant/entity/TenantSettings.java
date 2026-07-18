package com.saas.core.tenant.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Reads/writes the "modules" section of Tenant.settings (tenant-model.md's schema:
 * {"modules": {"fitness": {"enabled": true, ...}}}) without disturbing any other key a module may
 * have already written there - a plain get/set on the raw JSON string would risk overwriting
 * sibling config (e.g. qr_token_ttl_seconds) that this class never even needs to know about.
 * Package-private: this is Tenant's own implementation detail, not a shared abstraction.
 */
final class TenantSettings {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MODULES_KEY = "modules";
    private static final String ENABLED_KEY = "enabled";

    private final ObjectNode root;

    private TenantSettings(ObjectNode root) {
        this.root = root;
    }

    static TenantSettings parse(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return new TenantSettings(node instanceof ObjectNode objectNode ? objectNode : MAPPER.createObjectNode());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Tenant.settings must always be valid JSON", exception);
        }
    }

    boolean isModuleEnabled(String module) {
        return root.path(MODULES_KEY).path(module).path(ENABLED_KEY).asBoolean(false);
    }

    void setModuleEnabled(String module, boolean enabled) {
        ObjectNode modules = objectChild(root, MODULES_KEY);
        ObjectNode moduleNode = objectChild(modules, module);
        moduleNode.put(ENABLED_KEY, enabled);
    }

    String getModuleConfig(String module, String key) {
        JsonNode value = root.path(MODULES_KEY).path(module).path(key);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    void setModuleConfig(String module, String key, String value) {
        ObjectNode modules = objectChild(root, MODULES_KEY);
        ObjectNode moduleNode = objectChild(modules, module);
        moduleNode.put(key, value);
    }

    String toJson() {
        try {
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize tenant settings", exception);
        }
    }

    private static ObjectNode objectChild(ObjectNode parent, String key) {
        JsonNode existing = parent.get(key);
        return existing instanceof ObjectNode objectNode ? objectNode : parent.putObject(key);
    }
}
