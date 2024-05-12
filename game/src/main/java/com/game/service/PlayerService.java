package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    public List<Player> getFilteredPlayersWithPaging(String name, String title, Race race, Profession profession,
                                                   Long after, Long before, Boolean banned,
                                                   Integer minExperience, Integer maxExperience,
                                                   Integer minLevel, Integer maxLevel,
                                           Integer pageNumber, Integer pageSize, PlayerOrder order) {

        return playerRepository.findAll(getFilteredPlayers(name, title, race, profession,
                        after, before, banned, minExperience, maxExperience, minLevel, maxLevel),
                PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()))).getContent();
    }
    public Integer getFilteredPlayersCount(String name, String title, Race race, Profession profession,
                                                     Long after, Long before, Boolean banned,
                                                     Integer minExperience, Integer maxExperience,
                                                     Integer minLevel, Integer maxLevel) {

        return (int) playerRepository.count(getFilteredPlayers(name, title, race, profession,
                        after, before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }

    @Transactional
    public Player createPlayer(Player player){
        player.setLevel(calculateCurrentLevel(player));
        player.setUntilNextLevel(calculateUntilNextLevel(player));
        return playerRepository.save(player);
    }
    public Optional<Player> findPlayerById(Long id){
        return playerRepository.findById(id);
    }

    @Transactional
    public void deletePlayerById(Long id){
        playerRepository.deleteById(id);
    }
    public boolean existsById(Long id){
        return playerRepository.existsById(id);
    }

    @Transactional
    public Player updatePlayerById(Long id, Player updatedPlayer){

        Player player = playerRepository.findById(id).get();

            if (updatedPlayer.getName() != null)
                player.setName(updatedPlayer.getName());

            if (updatedPlayer.getTitle() != null)
                player.setTitle(updatedPlayer.getTitle());

            if (updatedPlayer.getRace() != null)
                player.setRace(updatedPlayer.getRace());

            if (updatedPlayer.getProfession() != null)
                player.setProfession(updatedPlayer.getProfession());

            if (updatedPlayer.getBirthday() != null)
                player.setBirthday(updatedPlayer.getBirthday());

            if (updatedPlayer.getBanned() != null)
                player.setBanned(updatedPlayer.getBanned());

            if (updatedPlayer.getExperience() != null)
                player.setExperience(updatedPlayer.getExperience());

            player.setLevel(calculateCurrentLevel(player));
            player.setUntilNextLevel(calculateUntilNextLevel(player));

            playerRepository.save(player);

        return player;
    }

    public Specification<Player> getFilteredPlayers(String name, String title, Race race, Profession profession,
                                                    Long after, Long before, Boolean banned,
                                                    Integer minExperience, Integer maxExperience,
                                                    Integer minLevel, Integer maxLevel) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (title != null){
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (race != null){
                predicates.add(criteriaBuilder.equal(root.get("race"), race));
            }
            if (profession != null){
                predicates.add(criteriaBuilder.equal(root.get("profession"), profession));
            }
            if (after != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthday"),new Date(after)));
            }
            if (before != null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(before)));
            }
            if (minExperience != null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience));
            }
            if (maxExperience != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience));
            }
            if (minLevel != null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel));
            }
            if (maxLevel != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel));
            }
            if (banned != null){
                predicates.add(criteriaBuilder.equal(root.get("banned"), banned));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }



    public Integer calculateCurrentLevel(Player player){
        return (int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100;
    }

    public Integer calculateUntilNextLevel(Player player){
        return 50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience();
    }
}
