package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PermissionsGenerator {

    private final AdvancedProtection plugin;

    public PermissionsGenerator(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    public void generate() {
        File file = new File(plugin.getDataFolder(), "Permisos.txt");

        // Siempre regenerar para mantener actualizado
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("=================================================");
            writer.println("       AdvancedProtection - Lista de Permisos");
            writer.println("=================================================");
            writer.println("");
            writer.println("--- Permisos Generales ---");
            writer.println("advancedprotection.use       - Permite usar el plugin (default: true)");
            writer.println("advancedprotection.admin     - Acceso total a todas las protecciones y comandos admin");
            writer.println("advancedprotection.admin.manager - Permite abrir el panel de administración (/ap manager)");
            writer.println(
                    "advancedprotection.admin.logs - Permite ver el historial de logs de protecciones (/ap logs)");
            writer.println("advancedprotection.bypass    - Permite romper/construir en cualquier protección ajena");
            writer.println("");
            writer.println("--- Permisos de Límites (Rangos) ---");
            writer.println("El plugin verifica estos permisos de mayor a menor.");
            writer.println("Si un jugador tiene varios, se aplica el número más alto.");
            writer.println("");
            writer.println("advancedprotection.limit.unlimited  - Protecciones infinitas");
            writer.println("");
            writer.println("Ejemplos para Rangos:");
            for (int i = 1; i <= 10; i++) {
                writer.println("advancedprotection.limit." + i + "       - Permite tener hasta " + i + " protecciones");
            }
            writer.println("...");
            writer.println("advancedprotection.limit.100     - Permite tener hasta 100 protecciones");
            writer.println("");
            writer.println("Nota: Si el jugador no tiene ningún permiso de límite,");
            writer.println("se usará el valor 'default_limit' del config.yml.");
            writer.println("");
            writer.println("=================================================");

            plugin.getLogger().info("Archivo Permisos.txt generado correctamente.");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al generar Permisos.txt: " + e.getMessage());
        }
    }
}
