package net.dnadas.training_portal.service.utils.converter;

import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.group.GroupResponsePrivateDTO;
import net.dnadas.training_portal.dto.group.GroupResponsePublicDTO;
import net.dnadas.training_portal.dto.requests.GroupJoinRequestResponseDto;
import net.dnadas.training_portal.model.group.UserGroup;
import net.dnadas.training_portal.model.request.UserGroupJoinRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GroupConverter {
  private final UserConverter userConverter;

  public List<GroupResponsePublicDTO> getGroupResponsePublicDtos(List<UserGroup> userGroups) {
    return userGroups.stream().map(
      this::getGroupResponsePublicDto).collect(Collectors.toList());
  }

  public GroupResponsePrivateDTO getGroupResponsePrivateDto(UserGroup userGroup) {
    return new GroupResponsePrivateDTO(
      userGroup.getId(), userGroup.getName(),
      userGroup.getDescription(), userGroup.getDetailedDescription());
  }

  public GroupResponsePublicDTO getGroupResponsePublicDto(UserGroup userGroup) {
    return new GroupResponsePublicDTO(
      userGroup.getId(), userGroup.getName(),
      userGroup.getDescription());
  }

  public GroupJoinRequestResponseDto getGroupJoinRequestResponseDto(
    UserGroupJoinRequest request) {
    return new GroupJoinRequestResponseDto(request.getId(),
      getGroupResponsePublicDto(request.getUserGroup()),
      userConverter.toUserResponsePublicDto(request.getApplicationUser()), request.getStatus());
  }

  public List<GroupJoinRequestResponseDto> getGroupJoinRequestResponseDtos(
    List<UserGroupJoinRequest> requests) {
    return requests.stream().map(this::getGroupJoinRequestResponseDto).collect(
      Collectors.toList());
  }
}
