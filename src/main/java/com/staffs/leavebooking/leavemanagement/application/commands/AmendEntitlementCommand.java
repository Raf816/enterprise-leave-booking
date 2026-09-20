package com.staffs.leavebooking.leavemanagement.application.commands;

public record AmendEntitlementCommand(
        String leaveAllowanceId,
        int newEntitlement
) {
}
