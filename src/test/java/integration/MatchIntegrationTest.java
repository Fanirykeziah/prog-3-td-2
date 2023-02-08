package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import app.foot.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
@Slf4j
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping
    private int MATCH_ID = 3;

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }

    @Test
    void read_matches_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<Match> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(actual.size(), 3);
        assertTrue(actual.contains(expectedMatch2()));
    }

    @Test
    void create_goals_ok() throws Exception {
        PlayerScorer toCreate = PlayerScorer.builder()
                .player(player1())
                .scoreTime(5)
                .isOG(false)
                .build();
        MockHttpServletResponse response = mockMvc.perform(post("/matches/" + MATCH_ID + "/goals")
                        .content(objectMapper.writeValueAsString(List.of(toCreate)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, actual.getTeamA().getScorers().size());
        assertEquals(1, actual.getTeamA().getScore());
    }

    @Test
    void create_goals_ko() {
        int MATCH_ID = 3;
        PlayerScorer toCreate = PlayerScorer.builder()
                .player(player1())
                .scoreTime(100)
                .isOG(false)
                .build();
        Exception exception = assertThrows(Exception.class, () -> {
            MockHttpServletResponse responses = mockMvc
                    .perform(post("/matches/" + MATCH_ID + "/goals")
                            .content(objectMapper.writeValueAsString(List.of(toCreate)))
                            .contentType("application/json")
                            .accept("application/json"))
                    .andReturn()
                    .getResponse();
        });
        assertEquals(BadRequestException.class, exception.getCause().getClass());
        assertEquals(
                "400 BAD_REQUEST : Player#J1 cannot score before after minute 90.",
                exception.getCause().getMessage()
        );
    }

    private static Match expectedMatch1() {
        return Match.builder()
                .id(1)
                .teamA(teamMatchAM1())
                .teamB(teamMatchBM1())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchAM2())
                .teamB(teamMatchBM2())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static Match expectedMatch3() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchAM3())
                .teamB(teamMatchBM3())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static TeamMatch teamMatchAM1() {
        return TeamMatch.builder()
                .team(team1())
                .score(4)
                .scorers(List.of(
                        PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(10)
                                .isOG(false)
                                .build() ,
                        PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(20)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(30)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player4())
                                .scoreTime(50)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static TeamMatch teamMatchBM1() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(
                        PlayerScorer.builder()
                                .player(player2())
                                .scoreTime(40)
                                .isOG(true)
                                .build(),
                        PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(50)
                                .isOG(false)
                                .build()
                ))
                .build();
    }

    private static TeamMatch teamMatchBM2() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchAM2() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static TeamMatch teamMatchAM3() {
        return TeamMatch.builder()
                .team(team1())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchBM3() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("J6")
                .teamName("E3")
                .isGuardian(false)
                .build();
    }

    private static Player player1() {
        return Player.builder()
                .id(1)
                .name("J1")
                .teamName("E1")
                .isGuardian(false)
                .build();
    }

    private static Player player2() {
        return Player.builder()
                .id(2)
                .name("J2")
                .teamName("E2")
                .isGuardian(false)
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .teamName("E2")
                .isGuardian(false)
                .build();
    }

    private static Player player4() {
        return Player.builder()
                .id(4)
                .name("J4")
                .teamName("E2")
                .isGuardian(false)
                .build();
    }

    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }

    private static Team team1() {
        return Team.builder()
                .id(1)
                .name("E1")
                .build();
    }

    private List<Match> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
}
