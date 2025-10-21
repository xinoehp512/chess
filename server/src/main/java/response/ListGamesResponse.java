package response;

import models.GameData;

import java.util.List;

public record ListGamesResponse(List<GameData> games) {
}
