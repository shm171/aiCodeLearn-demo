package com.mylab.ailearn.base.user.mapper;

import com.mylab.ailearn.base.entity.AppUser;
import com.mylab.ailearn.base.entity.Profile;
import com.mylab.ailearn.base.user.dtos.sendback.ProfileDto;
import com.mylab.ailearn.base.user.dtos.sendback.UserDto;
import com.mylab.ailearn.base.user.dtos.sendto.ProfileUpdateRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserRegisterRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // id 和创建时间由数据库生成，注册请求中的 password 写入 passwordHash。
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "createdAt", ignore = true)
    AppUser toEntity(UserRegisterRequest request);

    // Profile 关联保存后的账号，注册角色会按同名枚举值转换成 UserRole。
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "username", source = "request.username")
    @Mapping(target = "role", source = "request.role")
    @Mapping(target = "createdAt", ignore = true)
    Profile toProfile(UserRegisterRequest request, AppUser user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget AppUser user);

    // 普通档案修改只更新同名的 username，账号关联和角色保持不变。
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateProfile(ProfileUpdateRequest request, @MappingTarget Profile profile);

    UserDto toDto(AppUser user);

    @Mapping(target = "userId", source = "user.id")
    ProfileDto toProfileDto(Profile profile);
}
