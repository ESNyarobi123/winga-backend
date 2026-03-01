package com.winga.mapper;

import com.winga.dto.response.UserProfileResponse;
import com.winga.dto.response.UserResponse;
import com.winga.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper: User entity <-> UserResponse / UserProfileResponse. */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "isVerified", source = "isVerified")
    @Mapping(target = "verificationStatus", source = "verificationStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "defaultCategoryId", source = "defaultCategoryId")
    UserResponse toResponse(User user);

    /** Alias for profile endpoints — same shape as UserResponse. */
    default UserProfileResponse toProfileResponse(User user) {
        UserResponse r = toResponse(user);
        return new UserProfileResponse(
                r.id(), r.fullName(), r.email(), r.phoneNumber(), r.role(),
                r.profileImageUrl(), r.bio(), r.isVerified(), r.createdAt());
    }
}
