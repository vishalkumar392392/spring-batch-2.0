1. we created spring boot project and added spring batch dependency. 
   Then created job and step instances using autowired job and step builder factories and ran the application.
2. We added mysql connector and removed h2 dependecy. we added datasource configuration in application.properties
   When we ran the job the tables and metadata is updated in the database.
3. Added code to receive parameters in step and logged them in console .
4. We added more steps and added them to the job and also we added shell script given by the author into the project
5. We added error code to check whether spring batch is gonna start the failed steps or not.
6. We conditionally add flow logic in a job by using transitions. 
7. We added job execution deciders to customize the exit status and used transistions accordingly.
8. We added fail() and stopped() methods in the main job flow to correctly handle the job batch status.