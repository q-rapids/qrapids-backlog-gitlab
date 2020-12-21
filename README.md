# qrapids-backlog-gitlab

The q-rapids-backlog-gitlab plugin is a java application base on Spring Framework providing a transformation service of Quality Resquirements generated by the Q-Rapids dashboard into Issue for GitLab project management tool.

### Configuration :

The plugin is configure by the `application.properties` file which if you are using Tomcat as a Web Server, is located at: `<Tomcat folder>\webapps\qrapids-backlog-gitlab-X.Y.Z\WEB-INF\classes`.

**gitlab.url =** `http://<BACKLOG IP>:<BACKLOG PORT>` 

**gitlab.secret =** `<BACKLOG AUTHENTICATION>`

To link this plugin with the Q-Rapids Dashboard application, the plugin provide a rest service dedicated to quality requirement generation and consult milestones/phases. The URL of this service must be configure on [qr-dashboard configuration file](https://github.com/q-rapids/qrapids-dashboard/wiki/Configuration-File).

### Documentation :

Example usage of the REST API endpoints after the plugin is running, is available in [qrapids Backlog Services](https://github.com/q-rapids/qrapids-dashboard/wiki/qrapids-backlog-Services) wiki section.
