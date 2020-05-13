$source_profile = "default"
$region = "eu-west-1"
$mfa_serial = "{MFA_USER_ARN}"
$role_arn = "{ROLE_ARN}"
$target_profile = "test"
$target_profile_path =  "$HOME\.aws\credentials"
$session_name = "test"

# Get token code 
$token_code = Read-Host -Prompt 'Enter MFA token:'

# Assume Role
$Response = (Use-STSRole -Region $region -RoleArn  $role_arn -RoleSessionName $session_name -ProfileName $source_profile -SerialNumber $mfa_serial -TokenCode $token_code).Credentials

# Export Crendentail as environment variable
$env:AWS_ACCESS_KEY_ID=$Response.AccessKeyId
$env:AWS_SECRET_ACCESS_KEY=$Response.SecretAccessKey
$env:AWS_SESSION_TOKEN=$Response.SessionToken

# Create Profile with Credentials
Set-AWSCredential -StoreAs $target_profile -ProfileLocation $target_profile_path -AccessKey $Response.AccessKeyId -SecretKey $Response.SecretAccessKey -SessionToken $Response.SessionToken

# Print expiration time
Write-Host("Credentials will expire at: " + $Response.Expiration)
	
(Get-ECRLoginCommand).Password | docker login --username AWS --password-stdin 410123189863.dkr.ecr.eu-west-1.amazonaws.com
 
mvn clean install dockerfile:build
	
docker tag nhsd/email-adapter:latest 410123189863.dkr.ecr.eu-west-1.amazonaws.com/sgh-email-adapter:latest

docker push 410123189863.dkr.ecr.eu-west-1.amazonaws.com/sgh-email-adapter:latest

docker run -p 8080:8080 nhsd/email-adapter:latest --restart always
