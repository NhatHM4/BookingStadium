package com.booking.stadium.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "API kiểm tra tình trạng hệ thống")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    @Operation(summary = "Kiểm tra MySQL + Redis")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        // Check MySQL
        try (Connection conn = dataSource.getConnection()) {
            health.put("mysql", Map.of(
                    "status", "UP",
                    "database", conn.getCatalog(),
                    "url", conn.getMetaData().getURL()
            ));
        } catch (Exception e) {
            health.put("mysql", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
            health.put("status", "DEGRADED");
        }

        // Check Redis
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection().ping();
            redisTemplate.opsForValue().set("health:check", "ok");
            String value = (String) redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");
            health.put("redis", Map.of(
                    "status", "UP",
                    "ping", pong,
                    "read_write", "ok"
            ));
        } catch (Exception e) {
            health.put("redis", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
            health.put("status", "DEGRADED");
        }

        return ResponseEntity.ok(health);
    }
}
