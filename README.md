# Email Adapter
## Overview
This service reads email with html attachment from source & convert into pdf.
Also send the an email with pdf attachment to destination.

It uses The Exchange Web Services (EWS) Java API.
By using the EWS Java API, you can access almost all the information stored in an Office 365, Exchange Online, or Exchange Server mailbox.

It uses jasypt to encrypt & decrypt email crentails.
Refer to following to encrypt http://www.jasypt.org/cli.html

http://www.jasypt.org/cli.html 
## Source Code Location
The repository for this project is located in a public GitHub space here: https://github.com/UECIT/email-adapter

## Build Steps
1. add {MFA_USER_ARN} in build.ps1 script
2. add {ROLE_ARN} in build.os1 script
3. run build.ps1 script from powershell

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
