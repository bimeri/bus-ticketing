## Database/Entity changes
* Dec 4th, 2020
    * User entity
        1. add isAgencyAdminIndicator to User entity
        2. add fullName and email to user entity
    * official Agency entity    
        1. add policy field to official agency table
        2. add logo in the official agency table
        3. add code in the official agency table
    * payment transaction entity    
        1. add serviceChargeAmount to the payment transaction entity
        2. add agencyAmount to the payment transaction entity
    * BookedJourney entity
        1. add isAgencyBooking to the entity    
* Dec 17th, 2020
    * Add RefundPaymentTransaction entity
* Dec 22nd, 2020
    * BookedJourney entity
        1. add agencyUser to BookedJourney entity (join column is 'agency_user_id') 
* Jan 24th, 2021
    * add 'AppAlertNotice' entity table
___________________________________________
* Jan 29th 2021
    * add flatCharge field in ServiceCharge Entity table
    * changed IDs to `PLATFORM_SERVICE_CHARGE,SMS_NOTIF`
-------------------------------------------
* Feb 5th 2021
    * add title to alert notification table
    * add sms_notification in booked_journey table
    
* Mar 13 2021
    * Add `code` to user table
    * Gene UUID code for existing users
    * Deploy latest api-security
