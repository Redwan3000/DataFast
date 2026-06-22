package com.arits.datafast.dto.auth;

import lombok.Data;


public record UserSessionData(
        int    userId,
        String userName,        // encrypted
        String userEmail,       // encrypted
        String userPhone,       // encrypted
        String userRole,
        int    companyId,
        String companyName,
        String companyEmail,
        String companyAddress
) {}