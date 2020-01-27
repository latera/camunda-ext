# Demo business process
## HydraOMS
* pizza_order - Pizza order process

# Credentials
* https://oms.test-bss.job.latera.ru - HydraOMS (user@example.com/changeme)
* https://bpm.test-bss.job.latera.ru - Camunda (user/changeme)

# Build
## Install [SDKman](https://sdkman.io/install)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

## Install Java 8+, Maven and Groovy
```bash
sdk install java 8.0.222
sdk install groovy 2.4.16
sdk install maven 3.6.0
```

## Copy sources to local machine
```bash
scp -r applicant@test-bss.job.latera.ru:~/demo_processes demo_processes
cd demo_processes/pizza_order
```

## Install Camunda Modeler
See instruction: https://camunda.com/download/modeler/

## Make changes in business process scheme, user forms and scripts
Use Camunda Modeler for editing file src/main/resources/pizza_order.bpm and any text editor you prefer for editing src/main/resources/forms/* and src/main/resources/scripts/*.

## Build .war file
```bash
./build.sh
```

## Copy .war file to remote server for deployment
```bash
scp target/*.war applicant@test-bss.job.latera.ru:/etc/hydra/bpm-test/webapps
```

## Check deployment
Use Camunda web interface (Cockpit -> Deployments) for checking if deployment is successfully imported.
If something went wrong, check log file `/var/log/hydra/homs/bpm-test.log`. _Camunda checks bpm hash, so if nothing was changed, file will be skipped._

## Run Pizza Order process
Open Task List page in HydraOMS web interface.
Scroll to the bottom, press Add, choose Pizza Order, press Add, then press "Order Pizza" button.
Business process should start. Fill fields and use buttons of rendered form for moving through the entire process workflow.
