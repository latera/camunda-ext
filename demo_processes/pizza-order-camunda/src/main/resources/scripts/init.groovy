//Load app parameters from ENV variables
def ENV = System.getenv()

// camunda-ext
execution.setVariable("homsUrl",      "http://${ENV['HOMS_HOST']}:${ENV['HOMS_PORT']}")
execution.setVariable("homsUser",     ENV['HOMS_USER'])
execution.setVariable("homsPassword", ENV['HOMS_PASSWORD'])
execution.setVariable("hbwToken",     ENV['HOMS_HBW_TOKEN'])
