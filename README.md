# Lead Time Analyzer

The concept of Lead Time was based upon the book, [Accelerate: Building and Scaling
High Performing Technology Organizations](https://learning.oreilly.com/library/view/accelerate/9781457191435/)

This application is meant to calculate and provide API access Lead Time metrics
for a Git repository. This is meant to be used for teams that are not utilizing
Continuous Integration (CI) where changes merge to a primary branch at least
once per day.

This means that branches have changes which do not merge to a primary branch
and are therefore at risk of being outdated. This application analyzes those
merges to a primary branch and calculates statistics about how old they are so
these can be tracked over time.

The intent in creating this was to better understand how long it takes code to
land on a primary branch and eventually get released to customers. The longer
it takes to get code into the primary branch, the bigger the risk.

## Running

This is a Spring Boot Gradle project. The data needs to be read from a Git
repository and then imported into the application's database. This can be done
using a bootstrap script which bundles these steps below or they can be done
manually.

### Bootstrap
This will do the following:
- Remove the existing database
- Compile the application
- Create an application entry
- Load the application data

Run the following after substituting the appropriate values:
```
bin/bootstrap <application_name> /path/to/app/dir <master_branch_name> uses_teams_boolean
```

The master branch name should be the branch you wish to track where merges
are merged to. This is usually 'master'. The uses_teams_boolean should be
false unless you wish to inspect the team assignment code and utilize it to
assign merges to a specific team for reporting.

### Run the backend
Serve the backend API by running the following:

```
./gradlew backend
```

This will start the application at http://localhost:8080 which serves
an API that can be used.

### Reporting
- Run `bin/start.sh` to start the service
- Wait 10-20 seconds for the service to start
- Run `bin/generate-lead-time-plot -a app_name` to extract data and produce
  a plot chart. Run with `-h` to see the options that can be provided to shift
  dates or apply additional filter criteria.
- Run `bin/stop.sh` to stop the service.
