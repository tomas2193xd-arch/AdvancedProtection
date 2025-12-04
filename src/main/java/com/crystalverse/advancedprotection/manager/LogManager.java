package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LogManager {

    private final AdvancedProtection plugin;
    private final File logsFile;
    private final Gson gson;
    private final Map<UUID, List<ProtectionLog>> logs;
    private final int maxLogsPerProtection = 100; // Límite de logs por protección

    public LogManager(AdvancedProtection plugin) {
        this.plugin = plugin;
        this.logsFile = new File(plugin.getDataFolder(), "logs.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.logs = new HashMap<>();
        loadLogs();
    }

    public void addLog(ProtectionLog log) {
        UUID protectionId = log.getProtectionId();

        logs.putIfAbsent(protectionId, new ArrayList<>());
        List<ProtectionLog> protectionLogs = logs.get(protectionId);

        protectionLogs.add(log);

        // Mantener solo los últimos N logs
        if (protectionLogs.size() > maxLogsPerProtection) {
            protectionLogs.remove(0);
        }

        // Guardar asíncronamente
        saveLogs();
    }

    public List<ProtectionLog> getLogs(UUID protectionId) {
        return new ArrayList<>(logs.getOrDefault(protectionId, new ArrayList<>()));
    }

    public List<ProtectionLog> getLogs(UUID protectionId, int limit) {
        List<ProtectionLog> allLogs = getLogs(protectionId);
        int size = allLogs.size();
        if (size <= limit) {
            return allLogs;
        }
        return allLogs.subList(size - limit, size);
    }

    public List<ProtectionLog> getLogsByType(UUID protectionId, ProtectionLog.LogType type) {
        return getLogs(protectionId).stream()
                .filter(log -> log.getType() == type)
                .collect(Collectors.toList());
    }

    public List<ProtectionLog> getLogsByPlayer(UUID protectionId, UUID player) {
        return getLogs(protectionId).stream()
                .filter(log -> log.getPlayer().equals(player))
                .collect(Collectors.toList());
    }

    public void clearLogs(UUID protectionId) {
        logs.remove(protectionId);
        saveLogs();
    }

    public void clearAllLogs() {
        logs.clear();
        saveLogs();
    }

    private void loadLogs() {
        if (!logsFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(logsFile)) {
            Type type = new TypeToken<Map<UUID, List<ProtectionLog>>>() {
            }.getType();
            Map<UUID, List<ProtectionLog>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                logs.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error al cargar logs: " + e.getMessage());
        }
    }

    private void saveLogs() {
        try (Writer writer = new FileWriter(logsFile)) {
            gson.toJson(logs, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Error al guardar logs: " + e.getMessage());
        }
    }

    // Adapter para LocalDateTime
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}
