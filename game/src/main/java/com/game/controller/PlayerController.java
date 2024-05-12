package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAll(@RequestParam(value = "name",required = false) String name,
                               @RequestParam(value = "title",required = false) String title,
                               @RequestParam(value = "race",required = false) Race race,
                               @RequestParam(value = "profession",required = false) Profession profession,
                               @RequestParam(value = "before",required = false) Long after,
                               @RequestParam(value = "after",required = false) Long before,
                               @RequestParam(value = "banned",required = false) Boolean banned,
                               @RequestParam(value = "minExperience",required = false) Integer minExperience,
                               @RequestParam(value = "maxExperience",required = false) Integer maxExperience,
                               @RequestParam(value = "minLevel",required = false) Integer minLevel,
                               @RequestParam(value = "maxLevel",required = false) Integer maxLevel,
                               @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize,
                               @RequestParam(value = "order", required = false) PlayerOrder order){

        if (order == null){
            order = PlayerOrder.ID;
        }

        return ResponseEntity.ok(playerService.getFilteredPlayersWithPaging(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel, pageNumber, pageSize, order));
    }


    @GetMapping("/count")
    public ResponseEntity<Integer> count(@RequestParam(value = "name",required = false) String name,
                         @RequestParam(value = "title",required = false) String title,
                         @RequestParam(value = "race",required = false) Race race,
                         @RequestParam(value = "profession",required = false) Profession profession,
                         @RequestParam(value = "before",required = false) Long after,
                         @RequestParam(value = "after",required = false) Long before,
                         @RequestParam(value = "banned",required = false) Boolean banned,
                         @RequestParam(value = "minExperience",required = false) Integer minExperience,
                         @RequestParam(value = "maxExperience",required = false) Integer maxExperience,
                         @RequestParam(value = "minLevel",required = false) Integer minLevel,
                         @RequestParam(value = "maxLevel",required = false) Integer maxLevel){


        return ResponseEntity.ok(playerService.getFilteredPlayersCount(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }

    @PostMapping
    public ResponseEntity<Player> create(@RequestBody Player player){
        if (player.getBanned() == null){
            player.setBanned(false);
        }
        if (validatePlayer(player) || validatePlayerFields(player))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok(playerService.createPlayer(player));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long id){
        if (id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return playerService.findPlayerById(id).map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Player> delete(@PathVariable Long id){
        if (id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (!playerService.existsById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        playerService.deletePlayerById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id, @RequestBody Player updatedPlayer){

        if (id <= 0 )
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (!playerService.existsById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (updatedPlayer.getName() == null && updatedPlayer.getTitle() == null
                && updatedPlayer.getRace() == null && updatedPlayer.getExperience() == null
                && updatedPlayer.getBirthday() == null && updatedPlayer.getProfession() == null)
            return new ResponseEntity<>(playerService.findPlayerById(id).get(), HttpStatus.OK);

        Player player = playerService.updatePlayerById(id, updatedPlayer);
        if (validatePlayerFields(player))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(player, HttpStatus.OK);

    }

    public boolean validatePlayer(Player player){
        return player.getName() == null || player.getTitle() == null || player.getRace() == null
                || player.getExperience() == null || player.getBirthday() == null || player.getProfession() == null;
    }

    public boolean validatePlayerFields(Player player){
        return player.getName().length() >= 12 || player.getTitle().length() >= 30 || player.getName().equals("")
                || player.getExperience() <= 0 || player.getExperience() >= 10000000
                || player.getBirthday().getYear() + 1900 <= 2000 || player.getBirthday().getYear() + 1900 >= 3000;
    }

}
