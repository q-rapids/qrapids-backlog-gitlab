# qrapids-backlog-gitlab

The q-rapids-backlog-gitlab plugin is a java application base on Spring Framework providing communication between Q-Rapids dashboard and GitLab (as a backlog manager tool). 

## Configuration

Some properties need to be configured in the `application.properties` file, this file (in the case of a Tomcat server) is located at: `<Tomcat folder>\webapps\qrapids-backlog-gitlab-X.Y.Z\WEB-INF\classes`.

* **gitlab.url =** `http://<BACKLOG IP>:<BACKLOG PORT>` 
* **gitlab.secret =** `<BACKLOG AUTHENTICATION>`

To link Q-Rapids Dashboard application to this plugin, the services END POINTs must be configured on the [qr-dashboard configuration file](https://github.com/q-rapids/qrapids-dashboard/wiki/Configuration-File).

## Documentation

The REST API services included in this component are:
* **createIssue**:Export QR service 
* **milestones**: Import Milestones
* **phases**: Import Phases

Documentation is available at [qrapids Backlog Services](https://github.com/q-rapids/qrapids-dashboard/wiki/qrapids-backlog-Services) wiki section.
