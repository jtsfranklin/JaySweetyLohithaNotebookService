#!/bin/sh 
JSLDirectoryServicePath=./out/artifacts/JSLDirectoryServiceEar/JSLDirectoryServiceEar.ear
JSLNotebookServicePath=./out/artifacts/JSLNotebookService/JSLNotebookService.war
JSLNotebookService2Path=./out/artifacts/JSLNotebookService/JSLNotebookService2.war
JSLNotebookService3Path=./out/artifacts/JSLNotebookService/JSLNotebookService3.war

cp $JSLNotebookServicePath $JSLNotebookService2Path
cp $JSLNotebookServicePath $JSLNotebookService3Path

#time asadmin deploy --force=true --name JSLDirectoryServiceEar $JSLDirectoryServicePath
#time asadmin deploy --force=true --name JSLNotebookService $JSLNotebookServicePath
time asadmin deploy --force=true --name JSLNotebookService2 $JSLNotebookService2Path
time asadmin deploy --force=true --name JSLNotebookService3 $JSLNotebookService3Path
