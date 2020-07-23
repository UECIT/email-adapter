# Email Adapter
## Overview
This service reads email with html attachment from source & convert into pdf.
Also send the an email with pdf attachment to destination.

It uses The Exchange Web Services (EWS) Java API.
By using the EWS Java API, you can access almost all the information stored in an Office 365, Exchange Online, or Exchange Server mailbox.

## Configuration values stored in AWS parameter store with default vaules
iucds-environment -> dev

#ems
iucds-{env}-ems-email-username -> email username
iucds-{env}-ems-email-password -> email password

iucds-{env}-ems-email-subject ->	IUCDS Pilot
iucds-{env}-ems-email-body ->	Please find attached
iucds-{env}-ems-email-recipients ->  destination email recipients

iucds-{env}-ems-email-item-view-page-size -> 100
iucds-{env}-ems-email-from -> email address of sender

#mirth-connect
iucds-{env}-mirth-connect-tcp-host
iucds-{env}-mirth-connect-port-number

## Source Code Location
The repository for this project is located in a public GitHub space here: https://github.com/UECIT/email-adapter

## Licence

Unless stated otherwise, the codebase is released under [the MIT License][mit].
This covers both the codebase and any sample code in the documentation.

The documentation is [© Crown copyright][copyright] and available under the terms
of the [Open Government 3.0][ogl] licence.

[rvm]: https://www.ruby-lang.org/en/documentation/installation/#managers
[bundler]: http://bundler.io/
[mit]: LICENCE
[copyright]: http://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/
[ogl]: http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
