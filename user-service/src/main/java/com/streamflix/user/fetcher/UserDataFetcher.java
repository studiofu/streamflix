package com.streamflix.user.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@DgsComponent
public class UserDataFetcher {

    @Autowired
    private UserRepository userRepository;

    @DgsQuery
    public List<User> users() {
        return userRepository.findAll();
    }

    @DgsQuery
    public User user(@InputArgument String id) {
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }

    // Required for Apollo Federation (@key directive)
    @DgsEntityFetcher(name = "User")
    public User userEntityFetcher(Map<String, Object> values) {
        String id = (String) values.get("id");
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }
}
