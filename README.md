# Sequelizer
Sequelizer is a simple and specialized webapp to execute a SELECT query against an ORACLE database, extract it to a csv file and upload it to a SVN repository.
This is a "scratch your own itch" situation. While working in performance testing, the need arose to periodically create csv files containing testing information, mostly a list of users with specific permissions to use for testing of an application.
As the performance projects are usually kept in SVN as all development projects, these csv needed to be uploaded to SVN as well.
This is the logical attempt to automate this process.

The downside of this being a solution to a specific problem is that this is a very specific tool. I'm sure that it is very easily modifiable though, since it has minimal coding and only one workflow, so, don't be discouraged about the technical specifity.

#Tech
This app uses:
- ojdbc7 thin to connect to Oracle databases for extraction of business information.
- mysql to store the sql jobs configuration. This is not really necessary and any other database will do, specially since the persistence layer uses JPA which can generate the schema in a plethora of other DBMS.
- Spring Data Rest, which entails the usual suspects: Spring Boot, JPA, Jersey, Thymeleaf, and others as configured by Boot.
- Obviously Maven :)
- Jquery on the client side: So, ok, I'm a backend dev, so I'm not big on the new Javascript frameworks. So I decided to interact with the Rest endpoints exposed by Spring Data Rest by merely using a collection of jquery functions rather than a full flow in a stablished JS framework. I tried to get it working in an understandable way for me.
- Bootstrap, Font Awesome and Font Awesome animation.


