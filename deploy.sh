#!/bin/sh 
JSLDirectoryServicePath=./out/artifacts/JSLDirectoryServiceEar/JSLDirectoryServiceEar.ear
JSLNotebookServicePath=./out/artifacts/JSLNotebookService/JSLNotebookService.war
JSLNotebookService2Path=./out/artifacts/JSLNotebookService/JSLNotebookService2.war
JSLNotebookService3Path=./out/artifacts/JSLNotebookService/JSLNotebookService3.war

cp $JSLNotebookServicePath $JSLNotebookService2Path
cp $JSLNotebookServicePath $JSLNotebookService3Path

asadmin deploy --force=true --name JSLDirectoryServiceEar $JSLDirectoryServicePath
asadmin deploy --force=true --name JSLNotebookService $JSLNotebookServicePath
asadmin deploy --force=true --name JSLNotebookService2 $JSLNotebookService2Path
asadmin deploy --force=true --name JSLNotebookService3 $JSLNotebookService3Path
