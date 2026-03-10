package com.dmytrobilokha.zghadai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;

@ApplicationScoped
public class ConfigService {

    private static final String CONTENT_ROOT_PROPERTY = "zghadai.content.root";

    private String contentRoot;

    public ConfigService() {
        // CDI required constructor
    }

    @Inject
    public ConfigService(
            @ConfigProperty(name = CONTENT_ROOT_PROPERTY) String contentRoot) {
        this.contentRoot = Objects.requireNonNull(
                contentRoot, CONTENT_ROOT_PROPERTY + " property must be set, but got null");
    }

    public String getContentRoot() {
        return contentRoot;
    }

}
