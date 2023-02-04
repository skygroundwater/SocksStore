package com.socksstore.controllers;


import com.socksstore.models.socks.Socks;
import com.socksstore.services.SocksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/socks")
public class SocksController {

    private final SocksService socksService;

    public SocksController(SocksService socksService) {
        this.socksService = socksService;
    }

    @PostMapping()
    public ResponseEntity<Long> addNewSocks(@RequestBody Socks socks, @RequestParam Long quantity){
            socksService.addSocksToStore(socks, quantity);
            return ResponseEntity.ok(socksService.giveSameSocks(socks.getColor().getRussianName(), socks.getReallySize(), socks.getComposition()));
    }

    @GetMapping("/sameSocks/{color}&{size}&{composition}")
    public ResponseEntity<Object> getQuantityOfTheSameSocks(@PathVariable String color, @PathVariable Double size, @PathVariable Integer composition){
        long quantity = socksService.giveSameSocks(color, size, composition);
        return ResponseEntity.ok(quantity);
    }

    @PutMapping("/release")
    public ResponseEntity<Long> releaseTheSocks(@RequestBody Socks socks, @RequestParam Long quantity){
        socksService.removeSocksFromStore(socks,quantity);
        return ResponseEntity.ok(socksService.giveSameSocks(socks.getColor().getRussianName(),socks.getReallySize(), socks.getComposition()));
    }

    @DeleteMapping("/write-off")
    public ResponseEntity<Void> writeOffSocks(@RequestBody Socks socks, @RequestParam Long quantity){
        socksService.removeSocksFromStore(socks,quantity);
        return ResponseEntity.ok().build();
    }



}
