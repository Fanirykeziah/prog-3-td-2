package app.foot.repository.mapper;

import app.foot.model.Player;
import app.foot.model.PlayerScorer;
import app.foot.model.PlayerUpdate;
import app.foot.repository.MatchRepository;
import app.foot.repository.PlayerRepository;
import app.foot.repository.TeamRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.entity.PlayerScoreEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PlayerMapper {
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public Player toDomain(PlayerEntity entity) {
        return Player.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isGuardian(entity.isGuardian())
                .teamName(entity.getTeam().getName())
                .build();
    }

    public PlayerScorer toDomain(PlayerScoreEntity entity) {
        return PlayerScorer.builder()
                .player(toDomain(entity.getPlayer()))
                .minute(entity.getMinute())
                .isOwnGoal(entity.isOwnGoal())
                .build();
    }

    public PlayerScoreEntity toEntity(int matchId, PlayerScorer scorer) {
        return PlayerScoreEntity.builder()
                .player(playerRepository.findById(scorer.getPlayer().getId()).get())
                .match(matchRepository.findById(matchId).get())
                .ownGoal(scorer.getIsOwnGoal())
                .minute(scorer.getMinute())
                .build();
    }

    public PlayerEntity toEntity(Player domain) {
        return PlayerEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .team(teamRepository.findByName(domain.getTeamName()))
                .guardian(domain.getIsGuardian())
                .build();
    }

    public PlayerEntity toEntity(PlayerUpdate domain) {
        PlayerEntity entity = playerRepository.getReferenceById(domain.getId());
        if (domain.getName() != null)
            entity.setName(domain.getName());
        if (domain.getIsGuardian() != null)
            entity.setGuardian(domain.getIsGuardian());
        return entity;
    }
}
