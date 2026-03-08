package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminUserListResponse {
    private List<AdminUsers> adminUsers = new ArrayList<>();
    private int totalPage;
}
