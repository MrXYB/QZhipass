package org.microsoft.qintelipass.controllers;

import org.microsoft.qintelipass.entity.hotkey.HotkeyConfig;
import org.microsoft.qintelipass.entity.hotkey.HotkeyConfigID;
import org.microsoft.qintelipass.repository.HotkeyConfigRepository;
import org.microsoft.qintelipass.repository.HotkeyRepository;
import org.microsoft.qintelipass.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("api/v1/user/config")
public class UserConfigController {
    private final HotkeyConfigRepository hotkeyRepository;
    @Autowired
    public UserConfigController(HotkeyConfigRepository hotkeyRepository) {
        this.hotkeyRepository = hotkeyRepository;
    }
    @GetMapping("/hotkey")
    public ResponseEntity<?> getHotkeys(@RequestParam int keyIndex){
        SecurityUtil.requireAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();
        HotkeyConfigID id = new HotkeyConfigID(userId, keyIndex);
        Optional<HotkeyConfig> config = hotkeyRepository.findById(id);
        if(config.isPresent()){
            return ResponseEntity.ok(config.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/hotkey")
    public ResponseEntity<?> setKeyConfig(@RequestParam int keyIndex,
                                           @RequestParam String keyName){
        SecurityUtil.requireAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();
        HotkeyConfigID id = new HotkeyConfigID(userId, keyIndex);
        Optional<HotkeyConfig> existing = hotkeyRepository.findById(id);
        HotkeyConfig config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setFunctionKey(keyName);
        } else {
            config = HotkeyConfig
                    .builder()
                    .index(keyIndex)
                    .userId(userId)
                    .functionKey(keyName)
                    .build();
        }
        HotkeyConfig saved = hotkeyRepository.save(config);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/hotkey")
    public ResponseEntity<?> resetConfig(@RequestParam int keyIndex){
        SecurityUtil.requireAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();
        HotkeyConfigID id = new HotkeyConfigID(userId, keyIndex);
        try {
            hotkeyRepository.deleteById(id);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }
}
