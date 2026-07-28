package fun.bm.blinearworld.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load(Class<?> configClass) {
        Path configPath = getConfigPath(configClass);

        if (Files.exists(configPath)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
                applyJson(configClass, json);
                LOGGER.info("Loaded config from {}", configPath);
            } catch (Exception e) {
                LOGGER.error("Failed to load config from {}, using defaults", configPath, e);
            }
        } else {
            LOGGER.info("Config file not found at {}, generating default", configPath);
        }

        save(configClass);
    }

    public static void save(Class<?> configClass) {
        Path configPath = getConfigPath(configClass);
        try {
            JsonObject json = toJson(configClass);
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(json));
            LOGGER.info("Saved config to {}", configPath);
        } catch (Exception e) {
            LOGGER.error("Failed to save config to {}", configPath, e);
        }
    }

    private static Path getConfigPath(Class<?> configClass) {
        String fileName = toSnakeCase(configClass.getSimpleName()) + ".json";
        return FabricLoader.getInstance().getConfigDir().resolve(fileName);
    }

    private static String toSnakeCase(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static JsonObject toJson(Class<?> configClass) throws IllegalAccessException {
        JsonObject json = new JsonObject();
        for (Field field : configClass.getDeclaredFields()) {
            ConfigInfo info = field.getAnnotation(ConfigInfo.class);
            if (info == null) continue;

            field.setAccessible(true);
            Class<?> type = field.getType();
            Object value = field.get(null);

            if (type == int.class) {
                json.addProperty(info.name(), (Integer) value);
            } else if (type == long.class) {
                json.addProperty(info.name(), (Long) value);
            } else if (type == double.class) {
                json.addProperty(info.name(), (Double) value);
            } else if (type == float.class) {
                json.addProperty(info.name(), (Float) value);
            } else if (type == boolean.class) {
                json.addProperty(info.name(), (Boolean) value);
            } else if (type == String.class) {
                json.addProperty(info.name(), (String) value);
            } else {
                json.add(info.name(), GSON.toJsonTree(value));
            }
        }
        return json;
    }

    private static void applyJson(Class<?> configClass, JsonObject json) throws IllegalAccessException {
        for (Field field : configClass.getDeclaredFields()) {
            ConfigInfo info = field.getAnnotation(ConfigInfo.class);
            if (info == null) continue;

            JsonElement element = json.get(info.name());
            if (element == null) continue;

            field.setAccessible(true);
            Class<?> type = field.getType();

            try {
                if (type == int.class) {
                    field.setInt(null, element.getAsInt());
                } else if (type == long.class) {
                    field.setLong(null, element.getAsLong());
                } else if (type == double.class) {
                    field.setDouble(null, element.getAsDouble());
                } else if (type == float.class) {
                    field.setFloat(null, element.getAsFloat());
                } else if (type == boolean.class) {
                    field.setBoolean(null, element.getAsBoolean());
                } else if (type == String.class) {
                    field.set(null, element.getAsString());
                } else {
                    field.set(null, GSON.fromJson(element, type));
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read config key '{}', keeping default value", info.name(), e);
            }
        }
    }
}
