#!/bin/bash -eu

export DB_TYPE="${DB_TYPE:-postgresql}"
export DB_HOST="${DB_HOST:-${BPM_DB_HOST-}}"
export DB_PORT="${DB_PORT:-${BPM_DB_PORT-}}"
export DB_NAME="${DB_NAME:-${BPM_DB_NAME-}}"
export DB_DRIVER="${DB_DRIVER:-${BPM_DB_DRIVER-}}"
export DB_USERNAME="${DB_USERNAME:-${BPM_DB_USER-}}"
export DB_PASSWORD="${DB_PASSWORD:-${BPM_DB_PASSWORD-}}"

if [[ "$DB_TYPE" == "postgresql" ]]; then
  if [[ "x$DB_URL" == "x" ]]; then
    export DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
  fi
  if [[ "x$DB_DRIVER" == "x" ]]; then
    export DB_DRIVER="org.postgresql.Driver"
  fi
fi

XML_JDBC="//Resource[@name='jdbc/ProcessEngine']"
XML_DRIVER="${XML_JDBC}/@driverClassName"
XML_URL="${XML_JDBC}/@url"
XML_USERNAME="${XML_JDBC}/@username"
XML_PASSWORD="${XML_JDBC}/@password"
XML_MAXACTIVE="${XML_JDBC}/@maxActive"
XML_MINIDLE="${XML_JDBC}/@minIdle"
XML_MAXIDLE="${XML_JDBC}/@maxIdle"

if [ -z "$SKIP_DB_CONFIG" ]; then
  echo "Configure database"
  xmlstarlet ed -L \
    -u "${XML_DRIVER}" -v "${DB_DRIVER}" \
    -u "${XML_URL}" -v "${DB_URL}" \
    -u "${XML_USERNAME}" -v "${DB_USERNAME}" \
    -u "${XML_PASSWORD}" -v "${DB_PASSWORD}" \
    -u "${XML_MAXACTIVE}" -v "${DB_CONN_MAXACTIVE}" \
    -u "${XML_MINIDLE}" -v "${DB_CONN_MINIDLE}" \
    -u "${XML_MAXIDLE}" -v "${DB_CONN_MAXIDLE}" \
    /camunda/conf/server.xml
fi

export HTTP_HOST="${HTTP_HOST:-${BPM_HTTP_HOST:-0.0.0.0}}"
export HTTP_PORT="${HTTP_PORT:-${BPM_HTTP_PORT:-8080}}"
export HTTP_TIMEOUT="${HTTP_TIMEOUT:-${BPM_HTTP_TIMEOUT:-20000}}"
export HTTP_PROTOCOL="${HTTP_PROTOCOL:-${BPM_HTTP_PROTOCOL:-http}}"
export HTTP_SECURE="${HTTP_SECURE:-${BPM_HTTP_SECURE:-false}}"
export HTTP_PROXY="${HTTP_PROXY:-${BPM_PROXY:-false}}"
export HTTP_PROXY_HOST="${HTTP_PROXY_HOST:-${BPM_PROXY_HOST-}}"
export HTTP_PROXY_PORT="${HTTP_PROXY_PORT:-${BPM_PROXY_PORT-}}"
export HTTP_PROXY_PROTOCOL="${HTTP_PROXY_PROTOCOL:-${BPM_PROXY_PROTOCOL-}}"
export HTTP_REDIRECT_PORT="${HTTP_REDIRECT_PORT:-${BPM_REDIRECT_PORT:-$HTTP_PORT}}"
export HTTP_CONNECT_TIMEOUT_SEC="${HTTP_CONNECT_TIMEOUT_SEC:-300}"
export HTTP_READ_TIMEOUT_SEC="${HTTP_READ_TIMEOUT_SEC:-300}"
export HTTP_WRITE_TIMEOUT_SEC="${HTTP_WRITE_TIMEOUT_SEC:-300}"

if [[ "x$HTTP_PROXY_HOST" != "x" || "x$HTTP_PROXY_PORT" != "x" || "x$HTTP_PROXY_PROTOCOL" != "x" ]]; then
  export HTTP_PROXY="true"
fi

if [[ "$HTTP_PROXY" == "true" && "x$HTTP_PROXY_PROTOCOL" != "x$HTTP_PROTOCOL" ]]; then
  export HTTP_PROTOCOL="$HTTP_PROXY_PROTOCOL"
fi

XML_CONNECTOR="//Connector[@protocol='HTTP/1.1']"
XML_HOST="${XML_CONNECTOR}/@address"
XML_PORT="${XML_CONNECTOR}/@port"
XML_TIMEOUT="${XML_CONNECTOR}/@connectionTimeout"
XML_SECURE="${XML_CONNECTOR}/@secure"
XML_PROTOCOL="${XML_CONNECTOR}/@scheme"
XML_PROXY_PORT="${XML_CONNECTOR}/@proxyPort"
XML_PROXY_HOST="${XML_CONNECTOR}/@proxyName"
XML_REDIRECT_PORT="${XML_CONNECTOR}/@redirectPort"

echo "Configure http listener"
xmlstarlet ed -L \
  -u "${XML_HOST}" -v "$HTTP_HOST" \
  -u "${XML_PORT}" -v "$HTTP_PORT" \
  -u "${XML_TIMEOUT}" -v "$HTTP_TIMEOUT" \
  -u "${XML_REDIRECT_PORT}" -v "${HTTP_REDIRECT_PORT:-8443}" \
    -u "${XML_SECURE}" -v "${HTTP_SECURE}" \
    -u "${XML_PROTOCOL}" -v "${HTTP_PROTOCOL}" \
  /camunda/conf/server.xml

if [[ "$HTTP_PROXY" == "true" ]]; then
  echo "Configure proxy http listener"
  xmlstarlet ed -L \
    -u "${XML_PROXY_PORT}" -v "${HTTP_PROXY_HOST}" \
    -u "${XML_PROXY_HOST}" -v "${HTTP_PROXY_PORT}" \
    -u "${XML_REDIRECT_PORT}" -v "${HTTP_PROXY_PORT}" \
    /camunda/conf/server.xml
fi

export HISTORY_LEVEL="${HISTORY_LEVEL:-${BPM_HISTORY_LEVEL:-none}}"

XML_PROPERTIES="//bpm-platform/process-engine/properties"
XML_HISTORY_LEVEL="${XML_PROPERTIES}[@name='history']"

echo "Configure bpm platform"
xmlstarlet ed -L \
  -u "${XML_HISTORY_LEVEL}" -v "${HISTORY_LEVEL}" \
  /camunda/conf/bpm-platform.xml

CMD="/camunda/bin/catalina.sh"
if [ "${DEBUG}" = "true" ]; then
  echo "Enabling debug mode, JPDA accessible under port 8000"

  export JPDA_ADDRESS="0.0.0.0:8000"
  CMD+=" jpda"
fi

CMD+=" run"

if [ -n "${WAIT_FOR}" ]; then
    CMD="wait-for-it.sh ${WAIT_FOR} -s -t ${WAIT_FOR_TIMEOUT} -- ${CMD}"
fi

export DB_SEED="${DB_SEED:-${BPM_DB_SEED:-true}}"

export ADMIN_USERNAME="${ADMIN_USERNAME:-${BPM_USER:-user}}"
export ADMIN_PASSWORD="${ADMIN_PASSWORD:-${BPM_PASSWORD:-changeme}}"
export ADMIN_EMAIL="${ADMIN_EMAIL:-${BPM_EMAIL:-${HOMS_USER:-user@example.com}}}"
export ADMIN_FIRST_NAME="${ADMIN_FIRST_NAME:-Super}"
export ADMIN_LAST_NAME="${ADMIN_LAST_NAME:-Admin}"

if [[ "$DB_SEED" != "true" ]]; then
  rm -rf /camunda/webapps/seed*

  if [[ -f "/camunda/demo/war.lst" ]]; then
    cat /camunda/demo/war.lst | xargs -I {} bash -c "rm -rf /camunda/webapps/{}*"
  fi
fi

exec ${CMD}
