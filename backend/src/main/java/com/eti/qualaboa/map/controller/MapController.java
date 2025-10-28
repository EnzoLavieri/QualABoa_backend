package com.eti.qualaboa.map.controller;

import com.eti.qualaboa.map.dto.PinDTO;
import com.eti.qualaboa.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MapController {

    private final MapService mapService;

    @GetMapping("/pins/nearby")
    public ResponseEntity<List<PinDTO>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(required = false) String keyword) {
        List<PinDTO> pins = mapService.getPinsNearby(lat, lng, radius, keyword);
        return ResponseEntity.ok(pins);
    }

    @GetMapping("/places/{placeId}")
    public ResponseEntity<Map<String, Object>> placeDetails(@PathVariable String placeId) {
        Map<String, Object> details = mapService.getPlaceDetailsCached(placeId);
        return ResponseEntity.ok(details);
    }
}
