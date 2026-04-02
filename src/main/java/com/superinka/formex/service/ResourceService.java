package com.superinka.formex.service;

import com.superinka.formex.model.Resource;
import com.superinka.formex.payload.request.ResourceRequest;
import com.superinka.formex.payload.response.ResourceResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface ResourceService {

    List<ResourceResponse> getByCourse(Long courseId);

    Resource create(Long courseId, ResourceRequest request, Jwt jwt);

    void delete(Long courseId, Long resourceId);
}
