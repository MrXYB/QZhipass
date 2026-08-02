package org.microsoft.qintelipass.controllers;

import org.microsoft.qintelipass.entity.hotkey.Hotkey;
import org.microsoft.qintelipass.repository.HotkeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/configs/registry")
public class ConfigurationRegistryController {
    private final HotkeyRepository hotkeyRepository;
    @Autowired
    public ConfigurationRegistryController(HotkeyRepository hotkeyRepository) {
        this.hotkeyRepository = hotkeyRepository;
    }

    @GetMapping("/hotkeys")
    private ResponseEntity<?> getHotkeyRegistry(){
        List<Hotkey> keys = hotkeyRepository.findAll();
        Map<Number, String> keyMap = new HashMap<>();
        for (Hotkey key : keys) {
            keyMap.put(key.getKeyId(), key.getKeyName());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "mapping", keyMap
        ));
    }
}
