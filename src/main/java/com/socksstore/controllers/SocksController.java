package com.socksstore.controllers;

import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.exceptions.NotEnoughSocksException;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.services.socksservice.SocksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/socks")
@Tag(name = "CONTROL SYSTEM FOR STOCKED SOCKS")
public class SocksController {

    private final SocksService socksService;

    public SocksController(SocksService socksService) {
        this.socksService = socksService;
    }

    @Operation(
            summary = "Acceptance of new socks to the warehouse"
    )
    @PostMapping()
    public ResponseEntity<String> addNewSocks(
            @RequestBody @Parameter(description = "Passing a value as a JSON object")
                                              SocksEntity socks,

            @RequestParam @Parameter(description = "Integer value of the number of socks to be added to the warehouse")
                                              Long quantity) {
        try {
            socksService.addSocksToStore(socks, quantity);
            return ResponseEntity.ok("And now we have " + socksService.giveSameSocks(socks.getColor().getNameToString(), socks.getReallySize(), socks.getComposition(), socks.getComposition())
                    + " socks of this type in the store");
        } catch (InvalidValueException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get the number of socks of a certain type")
    @GetMapping("/same")
    public ResponseEntity<String> getQuantityOfTheSameSocks(@RequestParam(name = "color")
                                                            @Parameter(description = "Color name") String color,
                                                            @RequestParam(name = "size")
                                                            @Parameter(description = "Floating value from 35.0 to 47.0") double size,
                                                            @RequestParam(name = "minComposition")
                                                            @Parameter(description = "Integer value from 0 to 100") int minComposition,
                                                            @RequestParam(name = "maxComposition")
                                                            @Parameter(description = "Integer value from 0 to 100. But more than minimum") int maxComposition,
                                                            Model model) {
        try {
            long quantity = socksService.giveSameSocks(color, size, minComposition, maxComposition);
            return ResponseEntity.ok("There are " + quantity
                    + " socks of this type");
        } catch (InvalidValueException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Realization of socks from the warehouse")
    @PutMapping("/release")
    public ResponseEntity<Object> releaseTheSocks(@RequestBody @Parameter(description = "Passing a value as a JSON object")
                                                  SocksEntity socks,
                                                  @RequestParam @Parameter(description = "Integer value of the number of socks to be released from the warehouse")
                                                  Long quantity) {
        try {
            socksService.releaseSocksFromStore(socks, quantity);
        } catch (NotEnoughSocksException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidValueException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("There are " + socksService.giveSameSocks(socks.getColor().getNameToString(), socks.getReallySize(), socks.getComposition(), socks.getComposition())
                + " socks of this type left");
    }

    @Operation(summary = "Writing off of socks from the warehouse")
    @DeleteMapping("/write-off")
    public ResponseEntity<Object> writeOffSocks(@RequestBody
                                                @Parameter(description = "Passing a value as a JSON object")
                                                SocksEntity socks,
                                                @RequestParam
                                                @Parameter(description = "Integer value of the number of socks to be written off from the warehouse")
                                                Long quantity,
                                                @RequestParam(name = "reason for writing off socks")
                                                @Parameter(description = "Describe the problem. Why socks should be decommissioned")
                                                String cause) {
        try {
            socksService.writeOffSocksFromStore(socks, quantity, cause);
        } catch (NotEnoughSocksException | InvalidValueException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Defective socks successfully written off");
    }
}