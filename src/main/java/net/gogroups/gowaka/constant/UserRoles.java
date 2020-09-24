package net.gogroups.gowaka.constant;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 6:24 PM <br/>
 */
public enum UserRoles {

    USERS,
    GW_ADMIN,        // - Can create an Agency and assign AGENCY_ADMIN
    AGENCY_ADMIN,    // - Can Assign Agency User's roles
    AGENCY_MANAGER,  // - can add cars and Journey
    AGENCY_OPERATOR, // - Can add and view Journey
    AGENCY_BOOKING,  // - Can view Journey reports and stats of the Agency
    AGENCY_CHECKING, // - Can checking passengers for a Journey

}
