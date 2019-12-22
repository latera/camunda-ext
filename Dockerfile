FROM camunda/camunda-bpm-platform:7.9.0

SHELL ["/bin/bash", "-c"]
USER root
RUN rm -rf /camunda/webapps/camunda-invoice \
           /camunda/webapps/examples \
           /camunda/lib/groovy-all-2.4.13.jar \
           /camunda/lib/mail-1.4.1.jar && \
    apk add wget curl busybox-extras -f

USER camunda
RUN sed -i 's/<!-- <filter>/<filter>/' /camunda/webapps/engine-rest/WEB-INF/web.xml && sed -i 's/<\/filter-mapping> -->/<\/filter-mapping>/' /camunda/webapps/engine-rest/WEB-INF/web.xml
COPY ./context.xml /camunda/conf/

COPY ./target/dependencies/*.jar /camunda/lib/
COPY ./demo_processes/*/target/*.war /camunda/webapps/
COPY ./target/camunda-ext-*.jar /camunda/lib/camunda-ext.jar

