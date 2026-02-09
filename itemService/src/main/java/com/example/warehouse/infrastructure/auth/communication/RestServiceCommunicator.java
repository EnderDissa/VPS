package com.example.warehouse.infrastructure.auth.communication;

import com.example.warehouse.infrastructure.auth.AuthRequest;
import com.example.warehouse.infrastructure.auth.UserDetailsEntity;

import com.example.warehouse.infrastructure.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class RestServiceCommunicator {

    @Autowired
    private final UserServiceClient userService;

    @Autowired
    public RestServiceCommunicator(
            UserServiceClient userService
    ) {
        this.userService = userService;
    }

    public Mono<AuthRequest> executeRequest(AuthRequest request) {
        try {
            Mono<AuthRequest> blockingWrapper;

            switch (request.getType()) {
                case GET_USER:
                    blockingWrapper = Mono.fromCallable(() -> {
                        try {
                            String login = (String) request.getPayload();
                            UserDetailsEntity entity = userService.checkUserAuth(login).block();

                            if (entity != null) {
                                UserDetails userDetails = entity.toUserDetails();
                                request.setResult(true, userDetails != null ? userDetails : "User conversion failed");
                            } else {
                                request.setResult(false, "User not found");
                            }
                            return request;
                        } catch (Exception e) {
                            return getServerErrorResult(request, e.getMessage());
                        }
                    });
                    break;

                default:
                    return Mono.just(getServerErrorResult(request, "Unsupported request type: " + request.getType()));
            }

            return blockingWrapper
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(e -> Mono.just(getServerErrorResult(request, e.getMessage())));

        } catch (Exception e) {
            return Mono.just(getServerErrorResult(request, e.getMessage()));
        }
    }

    private AuthRequest getServerErrorResult(AuthRequest request, String errorMessage) {
        request.setResult(false, "Service unavailable: " + errorMessage);
        return request;
    }
}