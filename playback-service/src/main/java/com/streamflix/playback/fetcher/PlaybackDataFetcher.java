package com.streamflix.playback.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.streamflix.playback.dto.PlayHistoryEntry;
import com.streamflix.playback.dto.PlaybackState;
import com.streamflix.playback.model.Movie;
import com.streamflix.playback.model.User;
import com.streamflix.playback.service.PlayHistoryService;
import com.streamflix.playback.service.PlaybackProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@DgsComponent
public class PlaybackDataFetcher {

    @Autowired
    private PlaybackProgressService playbackProgressService;

    @Autowired
    private PlayHistoryService playHistoryService;

    @DgsQuery
    public boolean playbackHealth() {
        return true;
    }

    @DgsMutation
    public PlaybackState updatePlaybackProgress(
        @InputArgument String movieId,
        @InputArgument Integer positionSeconds,
        @InputArgument Integer durationSeconds,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId
    ) {
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        if (movieId == null || movieId.isEmpty()) {
            throw new RuntimeException("movieId is required");
        }
        if (positionSeconds == null || positionSeconds < 0) {
            throw new RuntimeException("positionSeconds must be non-negative");
        }
        return playbackProgressService.upsert(loggedInUserId, movieId, positionSeconds, durationSeconds);
    }

    @DgsMutation
    public PlayHistoryEntry recordPlay(
        @InputArgument String movieId,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId
    ) {
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        if (movieId == null || movieId.isEmpty()) {
            throw new RuntimeException("movieId is required");
        }
        return playHistoryService.recordPlay(loggedInUserId, movieId);
    }

    @DgsData(parentType = "Movie", field = "myPlayback")
    public PlaybackState myPlayback(DgsDataFetchingEnvironment dfe,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId
    ) {
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            return null;
        }
        Movie movie = dfe.getSource();
        return playbackProgressService.findForUserAndMovie(loggedInUserId, movie.getId()).orElse(null);
    }

    @DgsData(parentType = "User", field = "continueWatching")
    public List<PlaybackState> continueWatching(DgsDataFetchingEnvironment dfe,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId
    ) {
        User user = dfe.getSource();
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            return Collections.emptyList();
        }
        if (!loggedInUserId.equals(user.getId())) {
            return Collections.emptyList();
        }
        return playbackProgressService.findContinueWatchingForUser(user.getId());
    }

    @DgsData(parentType = "User", field = "playHistory")
    public List<PlayHistoryEntry> playHistory(DgsDataFetchingEnvironment dfe,
        @InputArgument Integer limit,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId
    ) {
        User user = dfe.getSource();
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            return Collections.emptyList();
        }
        if (!loggedInUserId.equals(user.getId())) {
            return Collections.emptyList();
        }
        return playHistoryService.listForUser(user.getId(), limit);
    }

    @DgsEntityFetcher(name = "Movie")
    public Movie movieEntityFetcher(Map<String, Object> values) {
        String id = (String) values.get("id");
        return new Movie(id);
    }

    @DgsEntityFetcher(name = "User")
    public User userEntityFetcher(Map<String, Object> values) {
        String id = (String) values.get("id");
        return new User(id);
    }
}
