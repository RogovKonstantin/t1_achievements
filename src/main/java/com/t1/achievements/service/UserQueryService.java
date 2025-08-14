// com.t1.achievements.service.UserQueryService
package com.t1.achievements.service;

import com.t1.achievements.dto.PageResponse;
import com.t1.achievements.dto.UserListItemDto;
import com.t1.achievements.entity.Asset;
import com.t1.achievements.entity.User;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepo;

    private String assetUrl(Asset a) { return a == null ? null : "/assets/" + a.getId(); }

    @Transactional(readOnly = true)
    public PageResponse<UserListItemDto> listUsers(Pageable pageable) {
        Page<User> page = userRepo.findByActiveTrue(pageable);
        return PageResponse.from(page.map(u -> new UserListItemDto(
                u.getId(), u.getFullName(), u.getDepartment(), u.getPosition(), assetUrl(u.getAvatar())
        )));
    }
}
