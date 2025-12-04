package com.crystalverse.advancedprotection.api;

import com.crystalverse.advancedprotection.model.ProtectionBlock;
import java.util.List;
import java.util.UUID;

public interface IStorageProvider {
    boolean init();
    void shutdown();
    
    void saveProtection(ProtectionBlock protection);
    void deleteProtection(UUID protectionId);
    ProtectionBlock loadProtection(UUID protectionId);
    List<ProtectionBlock> loadAllProtections();
}
