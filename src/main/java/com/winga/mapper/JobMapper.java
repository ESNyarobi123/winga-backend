package com.winga.mapper;

import com.winga.dto.response.JobResponse;
import com.winga.dto.response.UserResponse;
import com.winga.entity.Job;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;

/** MapStruct mapper: Job entity -> JobResponse. */
@Mapper(componentModel = "spring")
public interface JobMapper {

    /** Builds JobResponse from Job with client and proposalCount (from service layer). */
    default JobResponse toResponse(Job job, UserResponse client, long proposalCount) {
        List<String> tags = (job.getTags() != null && !job.getTags().isBlank())
                ? Arrays.asList(job.getTags().split(","))
                : List.of();
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getBudget(),
                job.getDeadline(),
                job.getStatus(),
                tags,
                job.getCategory(),
                job.getExperienceLevel(),
                job.getViewCount(),
                proposalCount,
                client,
                job.getCreatedAt());
    }
}
