SET ArtifactPath=%~dp0%out\artifacts\JSLNotebookService\JSLNotebookService.war
echo %ArtifactPath%

call asadmin redeploy --name JSLNotebookService %ArtifactPath%
if %errorlevel% neq 0 exit /b %errorlevel%
call asadmin redeploy --name JSLNotebookService2 %ArtifactPath%
if %errorlevel% neq 0 exit /b %errorlevel%
call asadmin redeploy --name JSLNotebookService3 %ArtifactPath%
if %errorlevel% neq 0 exit /b %errorlevel%
