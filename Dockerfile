FROM camunda/camunda-bpm-platform:7.8.0

SHELL ["/bin/bash", "-c"]
USER root
RUN rm -rf /camunda/webapps/camunda-invoice /camunda/webapps/examples
RUN apk add wget curl busybox-extras -f

USER camunda

RUN sed -i 's/<!-- <filter>/<filter>/' /camunda/webapps/engine-rest/WEB-INF/web.xml && sed -i 's/<\/filter-mapping> -->/<\/filter-mapping>/' /camunda/webapps/engine-rest/WEB-INF/web.xml

RUN cd /camunda/lib && \
  wget https://repo1.maven.org/maven2/org/codehaus/groovy/modules/http-builder/http-builder/0.7.1/http-builder-0.7.1.jar && \
  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.6/httpclient-4.5.6.jar && \
  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.10/httpcore-4.4.10.jar && \
  wget https://repo1.maven.org/maven2/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar && \
  wget https://repo1.maven.org/maven2/net/sf/json-lib/json-lib/2.4/json-lib-2.4-jdk15.jar && \
  wget http://maven.thingml.org/thirdparty/org/eclipse/maven/org.apache.xml.resolver/3.8.0/org.apache.xml.resolver-3.8.0.jar && \
  wget https://repo1.maven.org/maven2/commons-lang/commons-lang/2.5/commons-lang-2.5.jar && \
  wget https://repo1.maven.org/maven2/net/sf/ezmorph/ezmorph/1.0.6/ezmorph-1.0.6.jar && \
  wget https://repo1.maven.org/maven2/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar && \
  wget https://repo1.maven.org/maven2/commons-beanutils/commons-beanutils/1.8.0/commons-beanutils-1.8.0.jar && \
  wget https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-json/2.4.13/groovy-json-2.4.13.jar && \
  wget https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-xmlrpc/0.8/groovy-xmlrpc-0.8.jar && \
  wget https://repo1.maven.org/maven2/commons-codec/commons-codec/1.11/commons-codec-1.11.jar && \
  wget https://repo1.maven.org/maven2/xml-apis/xml-apis/1.4.01/xml-apis-1.4.01.jar && \
  wget https://repo1.maven.org/maven2/xerces/xercesImpl/2.12.0/xercesImpl-2.12.0.jar
COPY ./demo_processes/*/target/*.war /camunda/webapps/
COPY ./build/camunda-latera-*.jar /camunda/lib/camunda-ext.jar

