FROM unicon/grouper-demo:2.2.2

MAINTAINER jgasper@unicon.net

# The source locations are added to the docker build temp by the gradle file
ADD libs/ /opt/apache-tomcat-6.0.44/webapps/grouper/WEB-INF/lib/
ADD classes/ /opt/apache-tomcat-6.0.44/webapps/grouper/WEB-INF/classes/
ADD /provisioningTargetAttributes.gsh /

RUN set -x; \
    service mysql start \
    && service slapd start \
    && cd /opt/grouper.apiBinary-2.2.2/ \
    && bin/gsh /provisioningTargetAttributes.gsh

# The following is set by the base image, but left here for reference.
# EXPOSE 389 3306 8080
# CMD ["run-grouper.sh"]